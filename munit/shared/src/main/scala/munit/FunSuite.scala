package munit

import munit.internal.console.StackTraces
import munit.internal.FutureCompat._

import scala.collection.mutable
import scala.util.Failure
import scala.util.Success
import scala.concurrent.duration.Duration
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

abstract class FunSuite
    extends Suite
    with Assertions
    with TestOptionsConversions { self =>

  final type TestValue = Any

  def isCI: Boolean = "true" == System.getenv("CI")
  def munitIgnore: Boolean = false
  def munitFlakyOK: Boolean = "true" == System.getenv("MUNIT_FLAKY_OK")
  def munitExecutionContext: ExecutionContext = ExecutionContext.global
  def munitExecuteNow: ExecutionContext = new ExecutionContext {
    def execute(runnable: Runnable): Unit = runnable.run()
    def reportFailure(cause: Throwable): Unit = cause.printStackTrace()
  }

  val munitTestsBuffer: mutable.ArrayBuffer[Test] =
    mutable.ArrayBuffer.empty[Test]
  def munitTests(): Seq[Test] = {
    if (munitIgnore) {
      Nil
    } else {
      val onlyTests = munitTestsBuffer.filter(_.tags(Only))
      if (onlyTests.nonEmpty) {
        if (isCI) {
          onlyTests.toSeq.map(t =>
            if (t.tags(Only)) {
              t.withBody[TestValue](() =>
                fail("'Only' tag is not allowed when `isCI=true`")(t.location)
              )
            } else {
              t
            }
          )
        } else {
          onlyTests.toSeq
        }
      } else {
        munitTestsBuffer.toSeq
      }
    }
  }

  private val defaultTimeout = Duration(30, "s")
  def munitTimeout: Duration = defaultTimeout
  def munitTestValue(testValue: => Any): Future[Any] = {
    val wrappedFuture = testValue match {
      case f: Future[_] => f
      case v            => Future(v)(munitExecutionContext)
    }
    def flattenFuture(future: Future[_]): Future[_] = {
      val nested = future.map {
        case f: Future[_] => flattenFuture(f)
        case x            => Future.successful(x)
      }(munitExecuteNow)
      nested.flatten
    }
    flattenFuture(wrappedFuture)
  }

  def munitNewTest(test: Test): Test =
    test

  def test(options: TestOptions)(
      body: => Any
  )(implicit loc: Location): Unit = {
    munitTestsBuffer += munitNewTest(
      new Test(
        options.name, { () =>
          munitRunTest(
            options,
            StackTraces.dropOutside {
              try {
                munitTestValue(body)
              } catch {
                case NonFatal(e) =>
                  Future.failed(e)
              }
            }
          )
        },
        options.tags.toSet,
        loc
      )
    )
  }

  def munitRunTest(
      options: TestOptions,
      body: => Future[Any]
  ): Future[Any] = {
    if (options.tags(Fail)) {
      munitExpectFailure(options, body)
    } else if (options.tags(Flaky)) {
      munitFlaky(options, body)
    } else if (options.tags(Ignore)) {
      Future.successful(Ignore)
    } else {
      body
    }
  }

  def munitFlaky(
      options: TestOptions,
      body: => Future[Any]
  ): Future[Any] = {
    body.transformTry {
      case Success(value) => Success(value)
      case Failure(exception) =>
        if (munitFlakyOK) {
          Success(new TestValues.FlakyFailure(exception))
        } else {
          throw exception
        }
    }(munitExecutionContext)
  }

  def munitExpectFailure(
      options: TestOptions,
      body: => Future[Any]
  ): Future[Any] = {
    body.transformTry {
      case Success(value) =>
        Failure(
          throw new FailException(
            munitLines.formatLine(
              options.location,
              "expected failure but test passed"
            ),
            options.location
          )
        )
      case Failure(exception) =>
        Success(())
    }(munitExecutionContext)
  }

  class FunFixture[T](
      val setup: TestOptions => T,
      val teardown: T => Unit
  ) {
    def test(options: TestOptions)(
        body: T => Any
    )(implicit loc: Location): Unit = {
      self.test(options) {
        val argument = setup(options)
        try body(argument)
        finally teardown(argument)
      }(loc)
    }
  }
  object FunFixture {
    def map2[A, B](a: FunFixture[A], b: FunFixture[B]): FunFixture[(A, B)] =
      new FunFixture[(A, B)](
        setup = { options =>
          (a.setup(options), b.setup(options))
        },
        teardown = {
          case (argumentA, argumentB) =>
            try a.teardown(argumentA)
            finally b.teardown(argumentB)
        }
      )
    def map3[A, B, C](
        a: FunFixture[A],
        b: FunFixture[B],
        c: FunFixture[C]
    ): FunFixture[(A, B, C)] =
      new FunFixture[(A, B, C)](
        setup = { options =>
          (a.setup(options), b.setup(options), c.setup(options))
        },
        teardown = {
          case (argumentA, argumentB, argumentC) =>
            try a.teardown(argumentA)
            finally {
              try b.teardown(argumentB)
              finally c.teardown(argumentC)
            }
        }
      )
  }

}

package munit

import scala.collection.mutable
import scala.util.Try
import scala.util.Failure
import scala.util.Success

abstract class FunSuite
    extends Suite
    with Assertions
    with TestOptionsConversions {

  final type TestValue = Any

  def isCI: Boolean = "true" == System.getenv("CI")
  def munitIgnore: Boolean = false
  def isFlakyFailureOk: Boolean = "true" == System.getenv("FUNSUITE_FLAKY_OK")

  val munitTestsBuffer = mutable.ArrayBuffer.empty[Test]
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

  def test(name: String, tag: Tag*)(
      body: => Any
  )(implicit loc: Location): Unit = {
    test(new TestOptions(name, tag.toSet, loc))(body)
  }

  def test(options: TestOptions)(
      body: => Any
  )(implicit loc: Location): Unit = {
    munitTestsBuffer += new Test(
      options.name,
      () => munitRunTest(options, StackTraces.dropOutside(body)),
      options.tags.toSet,
      loc
    )
  }

  def munitRunTest(
      options: TestOptions,
      body: => Any
  ): Any = {
    if (options.tags(Fail)) {
      munitExpectFailure(options, body)
    } else if (options.tags(Flaky)) {
      munitFlaky(options, body)
    } else if (options.tags(Ignore)) {
      Ignore
    } else {
      body
    }
  }

  def munitFlaky(
      options: TestOptions,
      body: => Any
  ): Any = {
    val result = Try(body)
    result match {
      case Success(value) => value
      case Failure(exception) =>
        if (isFlakyFailureOk) {
          new TestValues.FlakyFailure(exception)
        } else {
          throw exception
        }
    }
  }

  def munitExpectFailure(
      options: TestOptions,
      body: => Any
  ): Any = {
    val result = scala.util.Try(body)
    if (result.isSuccess) {
      fail("expected failure but test passed")(options.loc)
    }
  }

}

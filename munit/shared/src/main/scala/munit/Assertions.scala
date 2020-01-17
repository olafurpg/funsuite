package munit

import scala.reflect.ClassTag
import scala.util.control.NonFatal

// FIXME(gabro): Constructing a `org.junit.AssumptionViolatedException` causes Dotty to crash
// so we use our own Exception class to work around it.
// See https://github.com/lampepfl/dotty/issues/7990
class DottyBugAssumptionViolatedException(message: String)
    extends RuntimeException

object Assertions extends Assertions
trait Assertions {

  val munitLines = new Lines

  def assert(
      cond: Boolean,
      details: => Any = "assertion failed"
  )(implicit loc: Location): Unit = {
    StackTraces.dropInside {
      if (!cond) {
        fail(munitDetails(details))
      }
    }
  }

  def assume(
      cond: Boolean,
      details: => Any = "assumption failed"
  )(implicit loc: Location): Unit = {
    StackTraces.dropInside {
      if (!cond) {
        throw new DottyBugAssumptionViolatedException(munitDetails(details))
      }
    }
  }

  def assertNoDiff(
      obtained: String,
      expected: String,
      details: => Any = "diff assertion failed"
  )(implicit loc: Location): Unit = {
    StackTraces.dropInside {
      Diffs.assertNoDiff(
        obtained,
        expected,
        munitDetails(details),
        printObtainedAsStripMargin = true
      )
    }
  }

  def assertNotEqual[A, B](
      obtained: A,
      expected: B,
      details: => Any = "values are the same"
  )(implicit loc: Location, ev: A =:= B): Unit = {
    StackTraces.dropInside {
      if (obtained == expected) {
        fail(munitDetails(details))
      }
    }
  }

  def assertEqual[A, B](
      obtained: A,
      expected: B,
      details: => Any = "values are not the same"
  )(implicit loc: Location, ev: A =:= B): Unit = {
    StackTraces.dropInside {
      if (obtained != expected) {
        Diffs.assertNoDiff(
          munitDetails(obtained),
          munitDetails(expected),
          munitDetails(details),
          printObtainedAsStripMargin = false
        )
      }
    }
  }

  def intercept[T <: Throwable](
      body: => Any
  )(implicit ev: ClassTag[T], loc: Location): Unit = {
    try {
      body
      fail(
        s"expected exception of type '${ev.runtimeClass.getName()}' but body evaluated successfully"
      )
    } catch {
      case e: FailException => throw e
      case NonFatal(e) =>
        if (!ev.runtimeClass.isAssignableFrom(e.getClass())) {
          val obtained = e.getClass().getName()
          val expected = ev.runtimeClass.getName()
          throw new FailException(
            s"intercept failed, exception '$obtained' is not a subtype of '$expected",
            cause = e,
            isStackTracesEnabled = false,
            location = loc
          )
        }
    }
  }

  def fail(message: String, cause: Throwable)(
      implicit loc: Location
  ): Nothing = {
    throw new FailException(
      munitLines.formatLine(loc, message),
      cause,
      isStackTracesEnabled = true,
      location = loc
    )
  }
  def fail(message: String)(implicit loc: Location): Nothing = {
    throw new FailException(munitLines.formatLine(loc, message), loc)
  }

  def munitDetails(details: => Any): String = {
    details match {
      case null            => "null"
      case message: String => message
      case value           => munitPrint(value)
    }
  }

  def munitPrint(value: Any): String = {
    Printers.print(value)
  }
}

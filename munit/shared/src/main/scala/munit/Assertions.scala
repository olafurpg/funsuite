package munit

import munit.internal.console.{Lines, Printers, StackTraces}
import munit.internal.difflib.Diffs

import scala.reflect.ClassTag
import scala.util.control.NonFatal
import scala.collection.mutable
import munit.internal.console.AnsiColors
import org.junit.AssumptionViolatedException
import munit.internal.MacroCompat

object Assertions extends Assertions
trait Assertions extends MacroCompat.CompileErrorMacro {

  val munitLines = new Lines

  def munitAnsiColors: Boolean = true

  private def munitFilterAnsi(message: String): String =
    if (munitAnsiColors) message
    else AnsiColors.filterAnsi(message)

  def assert(
      cond: => Boolean,
      clue: => Any = "assertion failed"
  )(implicit loc: Location): Unit = {
    StackTraces.dropInside {
      val (isTrue, clues) = munitCaptureClues(cond)
      if (!isTrue) {
        fail(munitPrint(clue), clues)
      }
    }
  }

  def assume(
      cond: Boolean,
      clue: => Any = "assumption failed"
  )(implicit loc: Location): Unit = {
    StackTraces.dropInside {
      if (!cond) {
        throw new AssumptionViolatedException(munitPrint(clue))
      }
    }
  }

  def assertNoDiff(
      obtained: String,
      expected: String,
      clue: => Any = "diff assertion failed"
  )(implicit loc: Location): Unit = {
    StackTraces.dropInside {
      Diffs.assertNoDiff(
        obtained,
        expected,
        message => fail(message),
        munitPrint(clue),
        printObtainedAsStripMargin = true
      )
    }
  }

  def assertNotEquals[A, B](
      obtained: A,
      expected: B,
      clue: => Any = "values are the same"
  )(implicit loc: Location, ev: A =:= B): Unit = {
    StackTraces.dropInside {
      if (obtained == expected) {
        fail(munitPrint(clue))
      }
    }
  }

  /**
   * Asserts that two elements are equal using `==` equality.
   *
   * The "expected" value (second argument) must have the same type or be a
   * subtype of the "obtained" value (first argument). For example:
   * {{{
   *   assertEquals(Option(1), Some(1)) // OK
   *   assertEquals(Some(1), Option(1)) // Error: Option[Int] is not a subtype of Some[Int]
   * }}}
   *
   * Use `assertEquals[Any, Any](a, b)` as an escape hatch to compare two
   * values of different types. For example:
   * {{{
   *   val a: Either[List[String], Int] = Right(42)
   *   val b: Either[String, Int]       = Right(42)
   *   assertEquals[Any, Any](a, b) // OK
   *   assertEquals(a, b) // Error: Either[String, Int] is not a subtype of Either[List[String], Int]
   * }}}
   */
  def assertEquals[A, B](
      obtained: A,
      expected: B,
      clue: => Any = "values are not the same"
  )(implicit loc: Location, ev: B <:< A): Unit = {
    StackTraces.dropInside {
      if (obtained != expected) {
        Diffs.assertNoDiff(
          munitPrint(obtained),
          munitPrint(expected),
          message => fail(message),
          munitPrint(clue),
          printObtainedAsStripMargin = false
        )
        // try with `.toString` in case `munitPrint()` produces identical formatting for both values.
        val obtainedString = obtained.toString()
        val expectedString = expected.toString()
        Diffs.assertNoDiff(
          obtainedString,
          expectedString,
          message => fail(message),
          munitPrint(clue),
          printObtainedAsStripMargin = false,
          isTrimmed = true
        )
        if (obtainedString != expectedString &&
            obtainedString.trim() == expectedString.trim()) {
          Diffs.assertNoDiff(
            obtainedString,
            expectedString,
            message => fail(message),
            munitPrint(clue),
            printObtainedAsStripMargin = false,
            isTrimmed = false
          )
        }
        fail(
          s"values are not equal even if they have the same `toString()`: $obtained"
        )
      }
    }
  }

  def intercept[T <: Throwable](
      body: => Any
  )(implicit T: ClassTag[T], loc: Location): T = {
    runIntercept(None, body)
  }

  def interceptMessage[T <: Throwable](expectedExceptionMessage: String)(
      body: => Any
  )(implicit T: ClassTag[T], loc: Location): T = {
    runIntercept(Some(expectedExceptionMessage), body)
  }

  private def runIntercept[T <: Throwable](
      expectedExceptionMessage: Option[String],
      body: => Any
  )(implicit T: ClassTag[T], loc: Location): T = {
    try {
      body
      fail(
        s"expected exception of type '${T.runtimeClass.getName()}' but body evaluated successfully"
      )
    } catch {
      case e: FailException if !T.runtimeClass.isAssignableFrom(e.getClass()) =>
        throw e
      case NonFatal(e) =>
        if (T.runtimeClass.isAssignableFrom(e.getClass())) {
          if (expectedExceptionMessage.isEmpty || e.getMessage == expectedExceptionMessage.get)
            e.asInstanceOf[T]
          else {
            val obtained = e.getClass().getName()
            throw new FailException(
              s"intercept failed, exception '$obtained' had message '${e.getMessage}', which was different from expected message '${expectedExceptionMessage.get}'",
              cause = e,
              isStackTracesEnabled = false,
              location = loc
            )
          }
        } else {
          val obtained = e.getClass().getName()
          val expected = T.runtimeClass.getName()
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
      munitFilterAnsi(munitLines.formatLine(loc, message)),
      cause,
      isStackTracesEnabled = true,
      location = loc
    )
  }
  def fail(
      message: String,
      clues: Clues = new Clues(Nil)
  )(implicit loc: Location): Nothing = {
    throw new FailException(
      munitFilterAnsi(munitLines.formatLine(loc, message, clues)),
      loc
    )
  }

  def failSuite(
      message: String,
      clues: Clues = new Clues(Nil)
  )(implicit loc: Location): Nothing = {
    throw new FailSuiteException(
      munitFilterAnsi(munitLines.formatLine(loc, message, clues)),
      loc
    )
  }

  private val munitCapturedClues: mutable.ListBuffer[Clue[_]] =
    mutable.ListBuffer.empty
  def munitCaptureClues[T](thunk: => T): (T, Clues) =
    synchronized {
      munitCapturedClues.clear()
      val result = thunk
      (result, new Clues(munitCapturedClues.toList))
    }

  def clue[T](c: Clue[T]): T = synchronized {
    munitCapturedClues += c
    c.value
  }
  def clues(clue: Clue[_]*): Clues = new Clues(clue.toList)

  def munitPrint(clue: => Any): String = {
    clue match {
      case message: String => message
      case value           => Printers.print(value)
    }
  }

}

/*
 * Adapted from https://github.com/scala-js/scala-js, see NOTICE.md.
 */

package com.geirsson.junit

import org.junit._
import sbt.testing._

final class JUnitReporter(
    eventHandler: EventHandler,
    loggers: Array[Logger],
    settings: RunSettings,
    taskDef: TaskDef
) {
  private val isAnsiSupported = loggers.forall(_.ansiCodesSupported()) && settings.color

  def reportTestSuiteStarted(): Unit = {
    log(Info, formattedTestClass + ":")
  }
  def reportTestStarted(method: Option[String]): Unit = {
    if (settings.verbose) {
      log(Info, s"$method started")
    }
  }

  def reportTestIgnored(method: String): Unit = {
    if (settings.verbose) {
      log(Info, Ansi.c(s"==> i $method ignored", Ansi.YELLOW))
    }
    emitEvent(Some(method), Status.Ignored)
  }
  def reportAssumptionViolation(
      method: String,
      timeInSeconds: Double,
      e: Throwable
  ): Unit = {
    logTestException(
      infoOrDebug,
      "",
      Some(method),
      e,
      timeInSeconds
    )
    emitEvent(Some(method), Status.Skipped)
  }
  private def formatTime(elapsedSeconds: Double): String =
    Ansi.c("%.2fs".format(elapsedSeconds), Ansi.DARK_GREY)
  def reportTestPassed(method: String, elapsedSeconds: Double): Unit = {
    log(
      Info,
      Ansi.c(s"+ $method", Ansi.GREEN) + " " + formatTime(elapsedSeconds)
    )
    emitEvent(Some(method), Status.Success)
  }
  def reportTestFailed(
      method: String,
      ex: Throwable,
      elapsedSeconds: Double
  ): Unit = {
    log(
      Info,
      new StringBuilder()
        .append(
          Ansi.c(
            s"==> X ${taskDef.fullyQualifiedName()}.$method",
            Ansi.LIGHT_RED
          )
        )
        .append(" ")
        .append(formatTime(elapsedSeconds))
        .append(" ")
        .append(ex.getClass().getName())
        .append(": ")
        .append(ex.getMessage())
        .toString()
    )
    trace(ex)
    emitEvent(Some(method), Status.Failure)
  }

  def reportErrors(
      prefix: String,
      method: Option[String],
      timeInSeconds: Double,
      errors: List[Throwable]
  ): Unit = {
    def emit(t: Throwable): Unit = {
      logTestException(Error, prefix, method, t, timeInSeconds)
      trace(t)
    }

    if (errors.nonEmpty) {
      emit(errors.head)
      emitEvent(method, Status.Failure)
      errors.tail.foreach(emit)
    }
  }

  private def logTestInfo(
      level: Level,
      method: Option[String],
      msg: String,
      color: String
  ): Unit =
    log(level, formatTest(method, color))

  private def logTestException(
      level: Level,
      prefix: String,
      method: Option[String],
      ex: Throwable,
      timeInSeconds: Double
  ): Unit = {
    val logException = {
      !settings.notLogExceptionClass &&
      (settings.logAssert || !ex.isInstanceOf[AssertionError])
    }

    val fmtName = if (logException) {
      val name = {
        if (ex.isInstanceOf[AssumptionViolatedException])
          classOf[internal.AssumptionViolatedException].getName
        else
          ex.getClass.getName
      }

      formatClass(name, Ansi.RED) + ": "
    } else {
      ""
    }

    val m = formatTest(method, Ansi.RED)
    val msg =
      s"$prefix$m failed: $fmtName${ex.getMessage}, took $timeInSeconds sec"
    log(level, msg)
  }

  def reportRunFinished(
      failed: Int,
      ignored: Int,
      total: Int,
      timeInSeconds: Double
  ): Unit = {
    if (settings.verbose) {
      val msg = {
        Ansi.c("Test run finished: ", Ansi.BLUE) +
          Ansi.c(s"$failed failed", if (failed == 0) Ansi.BLUE else Ansi.RED) +
          Ansi.c(s", ", Ansi.BLUE) +
          Ansi.c(
            s"$ignored ignored",
            if (ignored == 0) Ansi.BLUE else Ansi.YELLOW
          ) +
          Ansi.c(s", $total total, ${timeInSeconds}s", Ansi.BLUE)
      }
      log(Info, msg)
    }
  }

  private def trace(t: Throwable): Unit = {
    if (!t.isInstanceOf[AssertionError] || settings.logAssert) {
      logTrace(t)
    }
  }

  private def infoOrDebug: Level =
    if (settings.verbose) Info
    else Debug

  private def formatTest(method: Option[String], color: String): String = {
    method.fold(formattedTestClass) { method =>
      val fmtMethod = Ansi.c(settings.decodeName(method), color)
      s"$formattedTestClass.$fmtMethod"
    }
  }

  private lazy val formattedTestClass =
    formatClass(taskDef.fullyQualifiedName, Ansi.GREEN)

  private def formatClass(fullName: String, color: String): String = {
    Ansi.c(fullName, color)
  }

  private def emitEvent(method: Option[String], status: Status): Unit = {
    val testName = method.fold(taskDef.fullyQualifiedName)(method =>
      taskDef.fullyQualifiedName + "." + settings.decodeName(method)
    )
    val selector = new TestSelector(testName)
    eventHandler.handle(new JUnitEvent(taskDef, status, selector))
  }

  def log(level: Level, s: String): Unit = {
    if (settings.useSbtLoggers) {
      for (l <- loggers) {
        val msg = filterAnsiIfNeeded(l, s)
        level match {
          case Debug => l.debug(msg)
          case Info  => l.info(msg)
          case Warn  => l.warn(msg)
          case Error => l.error(msg)
          case _     => l.error(msg)
        }
      }
    } else {
      level match {
        case Debug | Trace if !settings.verbose =>
        case _ =>
          println(filterAnsiIfNeeded(isAnsiSupported, s))
      }
    }
  }

  private def filterAnsiIfNeeded(l: Logger, s: String): String =
    filterAnsiIfNeeded(l.ansiCodesSupported(), s)
  private def filterAnsiIfNeeded(isColorSupported: Boolean, s: String): String =
    if (isColorSupported && settings.color) s
    else Ansi.filterAnsi(s)

  private def logTrace(t: Throwable): Unit = {
    val trace = t.getStackTrace.dropWhile { p =>
      p.getFileName != null && {
        p.getFileName.contains("StackTrace.scala") ||
        p.getFileName.contains("Throwables.scala")
      }
    }
    val testFileName = {
      if (settings.color) findTestFileName(trace)
      else null
    }
    val i = trace.indexWhere { p =>
      p.getFileName != null && p.getFileName.contains("JUnitExecuteTest.scala")
    } - 1
    val m = if (i > 0) i else trace.length - 1
    logStackTracePart(trace, m, trace.length - m - 1, t, testFileName)
  }

  private def logStackTracePart(
      trace: Array[StackTraceElement],
      m: Int,
      framesInCommon: Int,
      t: Throwable,
      testFileName: String
  ): Unit = {
    val m0 = m
    var m2 = m
    var top = 0
    var i = top
    while (i <= m2) {
      if (trace(i).toString.startsWith("org.junit.") ||
          trace(i).toString.startsWith("org.hamcrest.")) {
        if (i == top) {
          top += 1
        } else {
          m2 = i - 1
          var break = false
          while (m2 > top && !break) {
            val s = trace(m2).toString
            if (!s.startsWith("java.lang.reflect.") &&
                !s.startsWith("sun.reflect.")) {
              break = true
            } else {
              m2 -= 1
            }
          }
          i = m2 // break
        }
      }
      i += 1
    }

    for (i <- top to m2) {
      log(
        Error,
        "    at " +
          stackTraceElementToString(trace(i), testFileName)
      )
    }
    if (m0 != m2) {
      // skip junit-related frames
      log(Error, "    ...")
    } else if (framesInCommon != 0) {
      // skip frames that were in the previous trace too
      log(Error, "    ... " + framesInCommon + " more")
    }
    logStackTraceAsCause(trace, t.getCause, testFileName)
  }

  private def logStackTraceAsCause(
      causedTrace: Array[StackTraceElement],
      t: Throwable,
      testFileName: String
  ): Unit = {
    if (t != null) {
      val trace = t.getStackTrace
      var m = trace.length - 1
      var n = causedTrace.length - 1
      while (m >= 0 && n >= 0 && trace(m) == causedTrace(n)) {
        m -= 1
        n -= 1
      }
      log(Error, "Caused by: " + t)
      logStackTracePart(trace, m, trace.length - 1 - m, t, testFileName)
    }
  }

  private def findTestFileName(trace: Array[StackTraceElement]): String =
    trace
      .find(_.getClassName == taskDef.fullyQualifiedName)
      .map(_.getFileName)
      .orNull

  private def stackTraceElementToString(
      e: StackTraceElement,
      testFileName: String
  ): String = {
    val highlight = settings.color && {
      taskDef.fullyQualifiedName == e.getClassName ||
      (testFileName != null && testFileName == e.getFileName)
    }
    var r = ""
    r += settings.decodeName(e.getClassName + '.' + e.getMethodName)
    r += '('

    if (e.isNativeMethod) {
      r += Ansi.c("Native Method", if (highlight) Ansi.YELLOW else null)
    } else if (e.getFileName == null) {
      r += Ansi.c("Unknown Source", if (highlight) Ansi.YELLOW else null)
    } else {
      r += Ansi.c(e.getFileName, if (highlight) Ansi.MAGENTA else null)
      if (e.getLineNumber >= 0) {
        r += ':'
        r += Ansi.c(
          String.valueOf(e.getLineNumber),
          if (highlight) Ansi.YELLOW else null
        )
      }
    }
    r += ')'
    r
  }
  private val Trace = 0
  private val Debug = 1
  private val Info = 2
  private val Warn = 3
  private val Error = 4
  private type Level = Int
}

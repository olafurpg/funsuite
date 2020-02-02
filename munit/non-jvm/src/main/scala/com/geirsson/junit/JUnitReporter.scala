/*
 * Adapted from https://github.com/scala-js/scala-js, see NOTICE.md.
 */

package com.geirsson.junit

import munit.internal.console.AnsiColors
import sbt.testing._
import munit.internal.PlatformCompat

final class JUnitReporter(
    eventHandler: EventHandler,
    loggers: Array[Logger],
    settings: RunSettings,
    taskDef: TaskDef
) {
  private val isAnsiSupported = loggers.forall(_.ansiCodesSupported()) && settings.color

  def reportTestSuiteStarted(): Unit = {
    log(
      Info,
      AnsiColors.c(s"${taskDef.fullyQualifiedName()}:", AnsiColors.GREEN)
    )
  }
  def reportTestStarted(method: String): Unit = {
    if (settings.verbose) {
      log(Info, s"$method started")
    }
  }

  def reportTestIgnored(method: String): Unit = {
    if (settings.verbose) {
      log(Info, AnsiColors.c(s"==> i $method ignored", AnsiColors.YELLOW))
    }
    emitEvent(method, Status.Ignored)
  }
  def reportAssumptionViolation(
      method: String,
      timeInSeconds: Double,
      e: Throwable
  ): Unit = {
    if (settings.verbose) {
      log(Info, AnsiColors.c(s"==> s $method skipped", AnsiColors.YELLOW))
    }
    emitEvent(method, Status.Skipped, new OptionalThrowable(e))
  }
  def reportTestPassed(method: String, elapsedMillis: Double): Unit = {
    log(
      Info,
      AnsiColors.c(s"+ $method", AnsiColors.GREEN) + " " +
        formatTime(elapsedMillis)
    )
    emitEvent(method, Status.Success)
  }
  def reportTestFailed(
      method: String,
      ex: Throwable,
      elapsedMillis: Double
  ): Unit = {
    log(
      Info,
      new StringBuilder()
        .append(
          AnsiColors.c(
            s"==> X ${taskDef.fullyQualifiedName()}.$method",
            AnsiColors.LightRed
          )
        )
        .append(" ")
        .append(formatTime(elapsedMillis))
        .append(" ")
        .append(ex.getClass().getName())
        .append(": ")
        .append(ex.getMessage())
        .toString()
    )
    trace(ex)
    emitEvent(method, Status.Failure, new OptionalThrowable(ex))
  }

  private def trace(t: Throwable): Unit = {
    if (!t.isInstanceOf[AssertionError] || settings.logAssert) {
      logTrace(t)
    }
  }

  private def emitEvent(
      method: String,
      status: Status,
      throwable: OptionalThrowable = new OptionalThrowable
  ): Unit = {
    val testName =
      taskDef.fullyQualifiedName + "." +
        settings.decodeName(method)
    val selector = new TestSelector(testName)
    eventHandler.handle(
      new JUnitEvent(taskDef, testName, status, selector, throwable)
    )
  }

  private def log(level: Level, s: String): Unit = {
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
    else AnsiColors.filterAnsi(s)

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
      log(Error, stackTraceElementToString(trace(i), testFileName))
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
      // This logic assumes that users have configured Scala.js sourcemaps.
      e.getFileName() != null &&
      e.getFileName().contains("file:/")
    }
    val canHighlight = !PlatformCompat.isNative
    new StringBuilder()
      .append(AnsiColors.Reset)
      .append(
        if (!canHighlight) ""
        else if (highlight) AnsiColors.Bold
        else AnsiColors.DarkGrey
      )
      .append("    at ")
      .append(settings.decodeName(e.getClassName + '.' + e.getMethodName))
      .append('(')
      .append(
        if (e.isNativeMethod()) {
          "Native Method"
        } else if (e.getFileName() == null) {
          "Unknown Source"
        } else {
          val file = e.getFileName().indexOf("file:/")
          val https =
            if (file >= 0) file else e.getFileName().indexOf("https:/")
          val filename =
            if (https >= 0) e.getFileName().substring(https)
            else e.getFileName()
          if (e.getLineNumber() >= 0) {
            s"${filename}:${e.getLineNumber()}"
          } else {
            filename
          }
        }
      )
      .append(')')
      .append(AnsiColors.Reset)
      .toString()
  }
  private def formatTime(elapsedMillis: Double): String =
    AnsiColors.c("%.2fs".format(elapsedMillis / 1000.0), AnsiColors.DarkGrey)
  private val Trace = 0
  private val Debug = 1
  private val Info = 2
  private val Warn = 3
  private val Error = 4
  private type Level = Int
}

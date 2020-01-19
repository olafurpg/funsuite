package com.geirsson.junit

import org.junit.runner.{Description, notification}
import org.junit.runner.notification.RunNotifier

import scala.collection.mutable
import sbt.testing.Logger

class MUnitRunNotifier(reporter: JUnitReporter) extends RunNotifier {
  var ignored = 0
  var total = 0
  var startedTimestamp = 0L
  val isFailed = mutable.Set.empty[String]
  override def fireTestSuiteStarted(description: Description): Unit = {
    reporter.reportTestSuiteStarted()
  }
  override def fireTestStarted(description: Description): Unit = {
    startedTimestamp = System.nanoTime()
    reporter.reportTestStarted(description.getMethodName)
  }
  def elapsedSeconds(): Double = {
    val elapsedNanos = System.nanoTime() - startedTimestamp
    elapsedNanos / 1000000000.0
  }
  override def fireTestIgnored(description: Description): Unit = {
    ignored += 1
    description.getMethodName match {
      case Some(methodName) =>
        reporter.reportTestIgnored(methodName)
      case None =>
        println(s"missing method name: ${description}")
    }
  }
  override def fireTestAssumptionFailed(
      failure: notification.Failure
  ): Unit = {
    failure.description.getMethodName match {
      case Some(methodName) =>
        reporter.reportAssumptionViolation(
          methodName,
          elapsedSeconds(),
          failure.ex
        )
      case None =>
        println(s"missing method name: ${failure.description}")
    }
  }
  override def fireTestFailure(failure: notification.Failure): Unit = {
    failure.description.getMethodName match {
      case Some(methodName) =>
        isFailed += methodName
        reporter.reportTestFailed(
          methodName,
          failure.ex,
          elapsedSeconds()
        )
      case None =>
        println(s"missing method name: ${failure.description}")
    }
  }
  override def fireTestFinished(description: Description): Unit = {
    description.getMethodName match {
      case Some(methodName) =>
        total += 1
        if (!isFailed(methodName)) {
          reporter.reportTestPassed(
            methodName,
            elapsedSeconds()
          )
        }
      case None =>
        println(s"missing method name: $description")
    }
  }
  override def fireTestSuiteFinished(description: Description): Unit = {
    reporter.reportRunFinished(
      isFailed.size,
      ignored,
      total,
      elapsedSeconds()
    )
  }

}

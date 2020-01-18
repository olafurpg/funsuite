package com.geirsson.junit

import org.junit.runner.{Description, notification}
import org.junit.runner.notification.RunNotifier

import scala.collection.mutable

class MUnitRunNotifier(reporter: Reporter) extends RunNotifier {
  var ignored = 0
  var total = 0
  var startedTimestamp = 0L
  val isFailed = mutable.Set.empty[String]
  override def fireTestSuiteStarted(description: Description): Unit = ()
  override def fireTestStarted(description: Description): Unit = {
    startedTimestamp = System.nanoTime()
    reporter.reportRunStarted()
  }
  def elapsedSeconds(): Double = {
    val elapsedNanos = System.nanoTime() - startedTimestamp
    elapsedNanos / 1000000000.0
  }
  override def fireTestIgnored(description: Description): Unit = {
    ignored += 1
    reporter.reportIgnored(description.getMethodName)
  }
  override def fireTestFinished(description: Description): Unit = {
    description.getMethodName match {
      case Some(methodName) =>
        total += 1
        if (!isFailed(methodName)) {
          reporter.reportTestFinished(
            methodName,
            succeeded = true,
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

  override def fireTestFailure(failure: notification.Failure): Unit = {
    failure.description.getMethodName match {
      case Some(methodName) =>
        isFailed += methodName
        failure.ex.printStackTrace()
        reporter.reportErrors(
          failure.description.getTestClass.fold("")(_.getName),
          failure.description.getMethodName,
          elapsedSeconds(),
          List(failure.ex)
        )
      //                reporter.reportTestFinished(
      //                  methodName,
      //                  succeeded = false,
      //                  elapsedSeconds()
      //                )
      case None =>
        println(s"missing method name: ${failure.description}")
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
}

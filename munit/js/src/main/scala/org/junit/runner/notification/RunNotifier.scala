package org.junit.runner.notification

import org.junit.runner.Description

trait RunNotifier {
  def fireTestStarted(description: Description): Unit
  def fireTestSuiteStarted(description: Description): Unit
  def fireTestSuiteFinished(description: Description): Unit
  def fireTestIgnored(description: Description): Unit
  def fireTestFinished(description: Description): Unit
  def fireTestFailure(failure: Failure): Unit
  def fireTestAssumptionFailed(failure: Failure): Unit
}

package org.junit.runner

import org.junit.runner.notification.RunNotifier
import org.junit.runner.Description

trait Runner {
  def run(notifier: RunNotifier): Unit
  def getDescription(): Description
}

package org.junit.runner

import org.junit.runner.notification.RunNotifier

trait Runner {
  def run(notifier: RunNotifier): Unit
}

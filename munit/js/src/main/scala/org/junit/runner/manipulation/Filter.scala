package org.junit.runner.manipulation

import org.junit.runner.Description

class Filter {
  def shouldRun(description: Description): Boolean = true
}
object Filter {
  def ALL: Filter = new Filter
}

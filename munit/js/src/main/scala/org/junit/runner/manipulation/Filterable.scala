package org.junit.runner.manipulation

import org.junit.runner.Description

trait Filterable {
  def filter(f: Filter): Unit
}

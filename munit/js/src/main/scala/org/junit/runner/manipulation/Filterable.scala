package org.junit.runner.manipulation

trait Filterable {
  def filter(f: Filter): Unit
}

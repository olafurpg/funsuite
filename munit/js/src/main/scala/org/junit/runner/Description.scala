package org.junit.runner

import java.lang.annotation.Annotation

class Description() {
  def addChild(description: Description): Description = this
}

object Description {
  def createSuiteDescription(cls: Class[_]): Description =
    new Description
  def createTestDescription(
      cls: Class[_],
      name: String,
      annotation: Annotation*
  ): Description =
    new Description
  def createTestDescription(cls: Class[_], name: String): Description =
    new Description
}

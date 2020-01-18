package org.junit.runner

import java.lang.annotation.Annotation

class Description(
    cls: Option[Class[_]] = None,
    methodName: Option[String] = None,
    annotations: List[Annotation] = Nil,
    children: List[Description] = Nil
) {
  def addChild(description: Description): Description =
    new Description(cls, methodName, annotations, description :: children)
  def getMethodName: Option[String] = methodName
  def getTestClass: Option[Class[_]] = cls
}

object Description {
  def createSuiteDescription(cls: Class[_]): Description =
    new Description(
      cls = Some(cls)
    )
  def createTestDescription(
      cls: Class[_],
      name: String,
      annotation: Annotation*
  ): Description =
    new Description(
      cls = Some(cls),
      methodName = Some(name),
      annotations = annotation.toList
    )
  def createTestDescription(cls: Class[_], name: String): Description =
    new Description(
      cls = Some(cls),
      methodName = Some(name)
    )
}

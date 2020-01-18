package com.geirsson.junit

import org.junit.runner.Runner
import sbt.testing.SubclassFingerprint

class CustomFingerprint(
    val suite: String,
    val isModule: Boolean
) extends SubclassFingerprint {
  override def superclassName(): String = suite
  override def requireNoArgConstructor(): Boolean = true
}

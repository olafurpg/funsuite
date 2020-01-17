package munit.internal

import sbt.testing.SubclassFingerprint

class MUnitFingerprint(val isModule: Boolean) extends SubclassFingerprint {
  def superclassName(): String = "munit.Suite"
  def requireNoArgConstructor(): Boolean = true
}

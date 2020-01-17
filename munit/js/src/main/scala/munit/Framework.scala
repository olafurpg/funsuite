package munit

import sbt.testing.Fingerprint
import sbt.testing.Runner
import sbt.testing.SubclassFingerprint
import munit.internal.MUnitFingerprint

class Framework extends sbt.testing.Framework {
  val underlying = new com.novocode.junit.JUnitFramework
  override val name = "munit"
  val munitFingerprint = new MUnitFingerprint(isModule = true)
  val fingerprints: Array[Fingerprint] = Array(
    munitFingerprint,
    new MUnitFingerprint(isModule = false)
  )
  def runner(
      args: Array[String],
      remoteArgs: Array[String],
      testClassLoader: ClassLoader
  ): Runner = {
    underlying.runner(args, remoteArgs, testClassLoader)
  }
  def slaveRunner(
      args: Array[String],
      remoteArgs: Array[String],
      testClassLoader: ClassLoader,
      send: String => Unit
  ): Runner = {
    underlying.slaveRunner(args, remoteArgs, testClassLoader, send)
  }
}

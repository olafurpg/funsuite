package munit

import sbt.testing.Fingerprint
import sbt.testing.Runner
import sbt.testing.SubclassFingerprint
import munit.internal.MUnitFingerprint

class Framework extends sbt.testing.Framework {
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
    new munit.internal.SbtRunner(remoteArgs, args, testClassLoader)
  }
  def slaveRunner(
      args: Array[String],
      remoteArgs: Array[String],
      testClassLoader: ClassLoader,
      send: String => Unit
  ): Runner = {
    new munit.internal.SbtRunner(remoteArgs, args, testClassLoader, Some(send))
  }
}

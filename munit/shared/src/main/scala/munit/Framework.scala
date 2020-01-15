package munit

import sbt.testing.Fingerprint

import com.geirsson.junit.CustomFingerprint
import com.geirsson.junit.CustomRunners
import sbt.testing.SubclassFingerprint

class Framework extends com.geirsson.junit.JUnitFramework {
  val munitFingerprint = CustomFingerprint.of(
    "munit.Suite",
    "munit.MUnitRunner"
  )
  override val fingerprints: Array[Fingerprint] = Array(
    munitFingerprint,
    new SubclassFingerprint {
      def isModule(): Boolean = true
      def superclassName(): String = "munit.Suite"
      def requireNoArgConstructor(): Boolean = true
    }
  )
  override val customRunners: CustomRunners =
    CustomRunners.of(munitFingerprint)
}

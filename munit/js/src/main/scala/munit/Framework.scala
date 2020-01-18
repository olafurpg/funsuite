package munit

import com.geirsson.junit.{CustomFingerprint, CustomRunners, JUnitFramework}

class Framework extends JUnitFramework {
  override val name = "munit"
  val munitFingerprint = new CustomFingerprint("munit.Suite", isModule = false)
  val customRunners = new CustomRunners(
    List(
      munitFingerprint,
      new CustomFingerprint("munit.Suite", isModule = true)
    )
  )
}

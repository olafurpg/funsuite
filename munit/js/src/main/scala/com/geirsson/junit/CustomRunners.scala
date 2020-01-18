package com.geirsson.junit

import sbt.testing.{Fingerprint, SubclassFingerprint}

class CustomRunners(val runners: List[CustomFingerprint]) {
  private val superclasses = runners.iterator.map(_.suite).toSet

  def matchesFingerprint(fingerprint: Fingerprint): Boolean =
    fingerprint match {
      case s: SubclassFingerprint => superclasses.contains(s.superclassName())
      case _                      => false
    }

}

package munit.internal.junitinterface

trait Settings {
  def trimStackTraces: Boolean
}

object Settings {
  def defaults(): Settings = new Settings {
    def trimStackTraces: Boolean = true
  }
}

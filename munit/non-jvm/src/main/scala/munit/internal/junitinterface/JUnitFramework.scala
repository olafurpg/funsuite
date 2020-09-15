/*
 * Adapted from https://github.com/scala-js/scala-js, see NOTICE.md.
 */

package munit.internal.junitinterface

import sbt.testing._

abstract class JUnitFramework extends Framework {

  val name: String = "Scala.js JUnit test framework"

  def customRunners: CustomRunners

  def fingerprints(): Array[Fingerprint] = {
    customRunners.runners.toArray
  }

  def runner(
      args: Array[String],
      remoteArgs: Array[String],
      testClassLoader: ClassLoader
  ): Runner = {
    new JUnitRunner(
      args,
      remoteArgs,
      parseRunSettings(args),
      testClassLoader,
      customRunners
    )
  }

  def slaveRunner(
      args: Array[String],
      remoteArgs: Array[String],
      testClassLoader: ClassLoader,
      send: String => Unit
  ): Runner = {
    new JUnitRunner(
      args,
      remoteArgs,
      parseRunSettings(args),
      testClassLoader,
      customRunners
    )
  }

  private def parseRunSettings(args: Array[String]): RunSettings = {
    val defaults = Settings.defaults()
    var verbose = false
    var noColor = false
    var decodeScalaNames = false
    var logAssert = false
    var notLogExceptionClass = false
    var useSbtLoggers = false
    var trimStackTraces = defaults.trimStackTraces
    var includeTags = Set.empty[String]
    var excludeTags = Set.empty[String]
    for (str <- args) {
      str match {
        case "-v" => verbose = true
        case "-n" => noColor = true
        case "-s" => decodeScalaNames = true
        case "-a" => logAssert = true
        case "-c" => notLogExceptionClass = true

        case s if s.startsWith("-tests=") =>
          throw new UnsupportedOperationException("-tests")

        case s if s.startsWith("--tests=") =>
          throw new UnsupportedOperationException("--tests")

        case s if s.startsWith("--ignore-runners=") =>
          throw new UnsupportedOperationException("--ignore-runners")

        case s if s.startsWith("--run-listener=") =>
          throw new UnsupportedOperationException("--run-listener")

        case s if s.startsWith("--exclude-tags=") =>
          excludeTags += s.stripPrefix("--exclude-tags=")

        case s if s.startsWith("--include-tags=") =>
          includeTags += s.stripPrefix("--include-tags=")

        case s if s.startsWith("--include-categories=") =>
          throw new UnsupportedOperationException("--include-categories")

        case s if s.startsWith("--exclude-categories=") =>
          throw new UnsupportedOperationException("--exclude-categories")

        case s if s.startsWith("-D") && s.contains("=") =>
          throw new UnsupportedOperationException("-Dkey=value")

        case s if !s.startsWith("-") && !s.startsWith("+") =>
          throw new UnsupportedOperationException(s)

        case _ =>
      }
    }
    for (s <- args) {
      s match {
        case "+v" => verbose = false
        case "+n" => noColor = false
        case "+s" => decodeScalaNames = false
        case "+a" => logAssert = false
        case "+c" => notLogExceptionClass = false
        case "+l" => useSbtLoggers = true
        case "-l" => useSbtLoggers = false
        case "+F" => trimStackTraces = true
        case "-F" => trimStackTraces = false
        case _    =>
      }
    }
    new RunSettings(
      color = !noColor,
      decodeScalaNames = decodeScalaNames,
      verbose = verbose,
      logAssert = logAssert,
      notLogExceptionClass = notLogExceptionClass,
      useSbtLoggers = useSbtLoggers,
      trimStackTraces = trimStackTraces,
      tags = new TagsFilter(includeTags, excludeTags)
    )
  }
}

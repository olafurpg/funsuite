---
id: getting-started
title: Getting started
---

MUnit is a Scala testing library with the following goals:

- **Reuse JUnit**: MUnit is implemented as a JUnit runner and tries to build on
  top of existing JUnit functionality where possible. Any tool that knows how to
  run a JUnit test suite knows how to run MUnit, including IDEs like IntelliJ.
- **Helpful console output**: test reports are pretty-printed with colors to
  help you quickly understand what caused a test failure. MUnit tries to
  displays diffs and source locations when possible and it does a best-effort to
  highlight relevant stack trace elements.
- **No Scala dependencies**: MUnit is implemented in ~1k lines of Scala code
  with no external Scala dependencies. The transitive Java dependencies weigh in
  total ~500kb, which is mostly just JUnit.

## Quick start

![Badge with version of the latest release](https://img.shields.io/maven-central/v/org.scalameta/munit_2.13?style=for-the-badge)

```scala
// Published for 2.11, 2.12 and 2.13. JVM-only.
libraryDependencies += "org.scalameta" %% "munit" % "@VERSION@"
testFrameworks += new TestFramework("munit.Framework")
```

Next, write a test suite.

```scala mdoc
class MySuite extends munit.FunSuite {
  test("hello") {
    val obtained = 42
    val expected = 43
    assertEqual(obtained, expected)
  }
}
```

```scala mdoc:invisible
import munit._
import docs.Docs._
```

## Usage

See the [usage guide](usage.md).

## Limitations

**JVM-only**: MUnit is currently only published for the JVM. MUnit uses a
[JUnit testing interface](https://github.com/olafurpg/junit-interface) for sbt
that's written in Java so that would need to be changed in order to add Scala.js
and Scala Native support. Feel free to open an issue if you would like to
contribute cross-platform support.

## Why JUnit?

MUnit is built on the idea that JUnit already great tooling integrations with
build tools like sbt and IDEs like IntelliJ. However, the JUnit testing syntax
is based on annotations and does not feel idiomatic when used from Scala. MUnit
tries to fill in the gap by providing a small Scala API on top of JUnit.

## Stability

MUnit is a new library with no stability guarantees. It's expected that new
releases, including patch releases, will have binary and source breaking
changes.

## Inspirations

MUnit is inspired by several existing testing libraries:

- ScalaTest: the syntax for defining `munit.FunSuite` test suites is the same as
  for `org.scalatest.FunSuite`.
- JUnit: MUnit is implemented as a custom JUnit runner and features like
  `assume` test filters are implemented on top of existing JUnit functionality.
- utest: the nicely formatted stack traces and test reports is heavily inspired
  by the beautifully formatted output in utest.
- ava: the idea for showing the source locations for assertion errors comes from
  [ava](https://github.com/avajs/ava), a JavaScript testing library.

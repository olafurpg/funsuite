# FunSuite

FunSuite is a Scala testing library with the following goals:

- **Reuse JUnit**: FunSuite is implemented as a JUnit runner and tries to build
  on top of existing JUnit functionality where possible. Any tool that knows how
  to run a JUnit test suite knows how to run FunSuite, including IDEs like
  IntelliJ.
- **Helpful console output**: test reports are pretty-printed with colors to
  help you quickly understand what caused a test failure. FunSuite tries to
  displays diffs and source locations when possible and it does a best-effort to
  highlight relevant stack trace elements.
- **No Scala dependencies**: FunSuite is implemented in ~1k lines of Scala code
  with no external Scala dependencies. The transitive Java dependencies weigh in
  total ~500kb, which is mostly just JUnit.

**Table of contents**

<!-- TOC depthFrom:2 -->

- [Getting started](#getting-started)
- [Features](#features)
  - [Source locations for assertion errors](#source-locations-for-assertion-errors)
  - [Highlighted stack traces](#highlighted-stack-traces)
  - [Multiline string diff](#multiline-string-diff)
- [Usage](#usage)
  - [Running logic before and after tests](#running-logic-before-and-after-tests)
  - [Skip test based on dynamic conditions](#skip-test-based-on-dynamic-conditions)
  - [Tag flaky tests](#tag-flaky-tests)
  - [Failing tests](#failing-tests)
  - [Running individual test](#running-individual-test)
  - [Ignore tests](#ignore-tests)
  - [Using JUnit categories](#using-junit-categories)
  - [Searching for failed tests in large log files](#searching-for-failed-tests-in-large-log-files)
  - [Running tests in IntelliJ](#running-tests-in-intellij)
- [Tests as values](#tests-as-values)
  - [Extend `Suit`](#extend-suit)
  - [Customize evaluation of tests](#customize-evaluation-of-tests)
  - [Filter tests based on dynamic conditions](#filter-tests-based-on-dynamic-conditions)
- [Limitations](#limitations)
- [Inspirations](#inspirations)
- [Do we really need another testing library?](#do-we-really-need-another-testing-library)
- [Coming from ScalaTest](#coming-from-scalatest)
- [Stability](#stability)

<!-- /TOC -->

## Getting started

![Badge with version of the latest release](https://img.shields.io/maven-central/v/com.geirsson/funsuite_2.13?style=for-the-badge)

```scala
// Published for 2.11, 2.12 and 2.13. JVM-only.
libraryDependencies += "com.geirsson" %% "funsuite" % "VERSION"
testFrameworks += new TestFramework("funsuite.Framework")
```

Next, write a test suite.

```scala
class MySuite extends funsuite.FunSuite {
  test("hello") {
    val obtained = 42
    val expected = 43
    assertEquals(obtained, expected)
  }
}
```

## Features

### Source locations for assertion errors

Assertion errors show the source code location where the assertion failed. Use
cmd+click on the location "`/path/to/BasicSuite.scala:36`" to open the exact
line number in your editor (may not work in all terminals).

![Source locations for assertion errors](https://i.imgur.com/6qhmz5F.png)

### Highlighted stack traces

Stack frame elements for classes that are defined in your project sources are
highlighted so you can focus on the important parts of the stack trace.

![Highlighted stack traces example](https://i.imgur.com/C6m6PbT.png)

### Multiline string diff

Use `assertNoDiff(obtained, expected)` to compare large multi-line strings.

![Multiline string diff](https://i.imgur.com/VY79UXX.png)

Test failures include the obtained multiline string in a copy-paste friendly
format making it easy to update the test as the expected behavior of your
program changes.

## Usage

### Running logic before and after tests

Override `beforeAll()`, `beforeEach()`, `afterAll()` and `afterEach()` to add
custom logic that should run before and after tests run. For example, use this
feature to create temporary files before executing tests or clean up acquired
resources after the test finish.

```scala
class MySuite extends funsuite.FunSuite {
  // Runs once before all tests start.
  override def beforeAll(context: BeforeAll): Unit = ???
  // Runs before each individual test.
  override def beforeEach(context: BeforeEach): Unit = ???
  // Runs after each individual test.
  override def afterEach(context: AfterEach): Unit = ???
  // Runs once after all tests have completed.
  override def afterAll(context: AfterAll): Unit = ???
}
```

### Skip test based on dynamic conditions

Use `assume(condition, explanation)` to skip tests when some conditions do not
hold. For example, use `assume` to conditionally run tests based on the
operating system or the Scala compiler version.

```scala
import scala.util.Properties
  test("paths") {
    assume(Properties.isLinux, "this test runs only on Linux")
    assume(Properties.versionNumberString.startsWith("2.13"), "this test runs only on Scala 2.13")
  }
```

### Tag flaky tests

Use `.flaky` to mark a test case that has a tendendency to fail sometimes.

```scala
  test("requests".flaky) {
    // I/O heavy tests that sometimes fail
  }
```

By default, flaky tests fail unless the `FUNSUITE_FLAKY_OK` environment variable
is set to `true`. Override the `isFlakyFailureOk` method to customize when it's
OK for flaky tests to fail.

### Failing tests

Use `.fail` to mark a test case that is expected to fail.

```scala
  test("issue-456".fail) {
    // Reproduce reported bug
  }
```

A failed test only succeeds if the test body fails. If the test body succeeds,
the test fails.

### Running individual test

Use `.only` to run only a single test.

```scala
  test("issue-457") {
    // will not run
  }
  test("issue-456".only) {
    // only test that runs
  }
  test("issue-455") {
    // will not run
  }
```

Use `testOnly -- $GLOB` to filter a fully qualified test name from the command
line.

```sh
# sbt shell
> testOnly -- *issue-456
```

Use `testOnly -- --only=$TEST_FILTER` to filter an individual test name from the
command line.

```sh
# sbt shell
> testOnly -- --only=issue-456
```

### Ignore tests

Use the `@Ignore` annotation to skip all tests in a test suite.

```scala
@funsuite.Ignore
class MySuite extends funsuite.FunSuite {
  test("hello1") {
    // will not run
  }
  test("hello2") {
    // will not run
  }
  // ...
}
```

Use `.ignore` to skip an individual test case in a test suite.

```scala
  test("issue-456".ignore) {
    // will not run
  }
```

### Using JUnit categories

Use `@Category(...)` to group tests suites together.

```scala
package myapp
import org.junit.experimental.categories.Category

class Slow extends funsuite.Tag("Slow")
class Fast extends funsuite.Tag("Fast")

@Category(Array(classOf[Slow]))
class MySlowSuite extends funsuite.FunSuite {
  test("slow") {
    Thread.sleep(1000)
  }
  // ...
}
@Category(Array(classOf[Slow], classOf[Fast]))
class MySlowFastSuite extends funsuite.FunSuite {
  // ...
}
@Category(Array(classOf[Fast]))
class MyFastSuite extends funsuite.FunSuite {
  // ...
}
```

Next, use `--include-category=$CATEGORY` and `--exclude-category=$CATEGORY` to
determine what test suites to run from the command line.

```sh
# matches: MySlowSuite, MySlowFastSuite
> testOnly -- --include-category=myapp.Slow

# matches: MySlowSuite
> testOnly -- --include-category=myapp.Slow --exclude-category=myapp.Fast
```

### Searching for failed tests in large log files

Test results are formatted in a specific way to make it easy to search for them
in a large log file.

| Test    | Prefix  |
| ------- | ------- |
| Failed  | `==> X` |
| Ignored | `==> i` |
| Success | `==> +` |
| Skipped | `==> s` |

Knowing these prefixes may come in handy for example when browsing test logs in
a browser. Search for "==> X" to quickly navigate to the failed tests.

### Running tests in IntelliJ

FunSuite test suites run in IntelliJ like normal.

![Running FunSuite from IntelliJ](https://i.imgur.com/oAA2ZeQ.png)

It's expected that it's not possible to run individual test cases from IntelliJ
since it does not understand the structure of the `test("name") {...}` syntax.
As a workaround, use the `.only` marker to run only a single test from IntelliJ.

```diff
- test("name") {
+ test("name".only) {
    // ...
  }
```

## Tests as values

FunSuite test cases are represented with the type `funsuite.Test` and can be
manipulated as a normal data structure. Feel free to tweak and extend how you
generate `funsuite.Test` to suit your needs.

### Extend `Suit`

Extend the base class `funsuite.Suite` to customize exactly what `Seq[Test]` you
want to run.

```scala
class MyCustomSuite extends funsuite.Suite {
  // The type returned by bodies of test cases.
  // Is defined as `Any` in `funsuite.FunSuite` but it's abstract in `funsuite.Suite`
  override type TestValue = Future[String]
  override def funsuiteTests() = List(
    new Test(
      "name",
      // compile error if it's not a Future[String]
      body = () => Future.successful("Hello world!"),
      tags = Set.empty[Tag],
      location = Location.generate
    )
  )
}
```

The abstract `funsuite.Suite` class only includes the before/after APIs and not
other methods like `assert` or `test()`.

### Customize evaluation of tests

Override `funsuiteRunTest()` to extend the default behavior for how test bodies
are evaluated. For example, use this feature to implement a `Rerun(N)` modifier
to evaluate the body multiple times.

```scala
import scala.util.Properties
case class Rerun(count: Int) extends Tag("Rerun")
class MyWindowsSuite extends funsuite.FunSuite {
  override def funsuiteRunTest(options: TestOptions, body: => Any): Any = {
    val rerunCount = options.tags.collectFirst {
      case Rerun(n) => n
    }.getOrElse(1)
    1.to(rerunCount).map(_ => super.funsuiteRunTest(options, body))
  }
  test("files", Rerun(10)) {
    println("Hello") // will run 10 times
  }
  test("files") {
    // will run once, like normal
  }
}
```

### Filter tests based on dynamic conditions

Override `funsuiteTests()` to customize what tests get executed. For example,
use this feature to skip tests based on a dynamic condition.

```scala
import scala.util.Properties
case object Windows extends funsuite.Tag("Windows")
class MyWindowsSuite extends funsuite.FunSuite {
  override def funsuiteTests(): Any = {
    val default = super.funsuiteTests()
    if (!Properties.isWin) default
    else default.filter(_.tags.contains(Windows))
  }
  test("files", Windows) {
    // will only run in Windows
  }
  test("files") {
    // will run like normal
  }
}
```

## Limitations

**JVM-only**: FunSuite is currently only published for the JVM. FunSuite uses a
[JUnit testing interface](https://github.com/olafurpg/junit-interface) for sbt
that's written in Java so that would need to be changed in order to add Scala.js
and Scala Native support. Feel free to open an issue if you would like to
contribute cross-platform support.

## Inspirations

FunSuite is inspired by several existing testing libraries:

- ScalaTest: the syntax for defining FunSuite test suites is the same as for
  `org.scalatest.FunSuite`.
- JUnit: FunSuite is implemented as a custom JUnit runner and features like
  `assume` test filters are implemented on top of existing JUnit functionality.
- utest: the nicely formatted stack traces and test reports is heavily inspired
  by the beautifully formatted output in utest.
- ava: the idea for showing the source locations for assertion errors comes from
  [ava](https://github.com/avajs/ava), a JavaScript testing library.

## Do we really need another testing library?

FunSuite is built on the idea that >90% of what a JVM testing library needs is
already provided by JUnit. However, the default JUnit testing syntax is based on
annotations and does not feel idiomatic when used from Scala. FunSuite tries to
fill in the gap by providing a small Scala API on top of JUnit.

## Coming from ScalaTest

Add the following settings to run ScalaTest and JUnit suites with the same
testing framework as FunSuite.

```scala
// build.sbt
testFrameworks := List(
  new TestFramework("funsuite.Framework"),
  new TestFramework("com.geirsson.junit.PantsFramework")
)
```

These settings configure all JUnit and ScalaTest suites to run with the same
testing interface as FunSuite. This means that you get the same pretty-printing
of test reports for JUnit, ScalaTest and FunSuite.

Next, you may want to start migrating your test suites one by one. If you only
use basic ScalaTest features, you should be able to replace usage of
`org.scalatest.FunSuite` with minimal changes like below.

```diff
- import org.scalatest.funsuite.AnyFunSuite
- import org.scalatest.FunSuite
+ import funsuite.FunSuite

- class MySuite extends FunSuite with BeforeAll with AfterAll {
+ class MySuite extends FunSuite {
  test("name") {
    // unchanged
  }

- ignore("ignored") {
+ test("ignored".ignore) {
    // unchanged
  }
```

## Stability

FunSuite is a new library with no stability guarantees. It's expected that new
releases, including patch releases, will have binary and source breaking
changes.

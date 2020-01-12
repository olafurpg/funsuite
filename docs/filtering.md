---
id: filtering
title: Filtering tests
---

MUnit provides several options for selecting what tests to run.

## Run only a single test case

Use `testOnly -- $GLOB` to filter a fully qualified test name from the command
line.

```sh
# sbt shell
> testOnly -- *issue-456
> testOnly -- com.foo.controllers.*
```

Use `testOnly -- --only=$TEST_FILTER` to filter an individual test name from the
command line.

```sh
# sbt shell
> testOnly -- --only=issue-456
```

Use `.only` to run only a single test without custom command-line arguments.

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

## Ignore single test case

Use `.ignore` to skip an individual test case in a test suite.

```scala
  test("issue-456".ignore) {
    // will not run
  }
```

## Ignore single test case based on a dynamic conditions

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

## Ignore entire test suite

Use the `@Ignore` annotation to skip all tests in a test suite.

```scala
@munit.Ignore
class MySuite extends munit.FunSuite {
  test("hello1") {
    // will not run
  }
  test("hello2") {
    // will not run
  }
  // ...
}
```

## Ignore entire test suite based on a dynamic condition

Override `munitIgnore: Boolean` to skip an entire test suite based on a dynamic
condition.

```scala
class MyWindowsSuite extends munit.FunSuite {
  override def munitIgnore: Boolean = !scala.util.Properties.isWin
  test("windows-only") {
    // only runs on Windows
  }
}
```

## Group test suites with categories

Use the experimental `@Category(...)` annotation from JUnit to group tests
suites together.

```scala
package myapp
import org.junit.experimental.categories.Category

class Slow extends munit.Tag("Slow")
class Fast extends munit.Tag("Fast")

@Category(Array(classOf[Slow]))
class MySlowSuite extends munit.FunSuite {
  test("slow") {
    Thread.sleep(1000)
  }
  // ...
}
@Category(Array(classOf[Slow], classOf[Fast]))
class MySlowFastSuite extends munit.FunSuite {
  // ...
}
@Category(Array(classOf[Fast]))
class MyFastSuite extends munit.FunSuite {
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

## Filter tests cases based on a dynamic conditions

Override `munitTests()` to customize what tests get executed. For example, use
this feature to skip tests based on a dynamic condition.

```scala
import scala.util.Properties
case object Windows extends munit.Tag("Windows")
class MyWindowsSuite extends munit.FunSuite {
  override def munitTests(): Any = {
    val default = super.munitTests()
    if (!Properties.isWin) default
    else default.filter(_.tags.contains(Windows))
  }
  test("files", Windows) {
    // will always run, including on Windows
  }
  test("files") {
    // will not run in Windows
  }
}
```

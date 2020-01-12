---
id: usage
title: Using MUnit
---

## Run logic before and after tests

Override `beforeAll()`, `beforeEach()`, `afterAll()` and `afterEach()` to add
custom logic that should run before and after tests run. For example, use this
feature to create temporary files before executing tests or clean up acquired
resources after the test finish.

```scala
class MySuite extends munit.FunSuite {
  // Runs once before all tests start.
  override def beforeAll(): Unit = ???
  // Runs before each individual test.
  override def beforeEach(): Unit = ???
  // Runs after each individual test.
  override def afterEach(context: AfterEach): Unit = ???
  // Runs once after all tests have completed.
  override def afterAll(context: AfterAll): Unit = ???
}
```

## Assert test fails

Use `.fail` to mark a test case that is expected to fail.

```scala
  test("issue-456".fail) {
    // Reproduce reported bug
  }
```

A failed test only succeeds if the test body fails. If the test body succeeds,
the test fails.

## Customize evaluation of tests

Override `munitRunTest()` to extend the default behavior for how test bodies are
evaluated. For example, use this feature to implement a `Rerun(N)` modifier to
evaluate the body multiple times.

```scala
import scala.util.Properties
case class Rerun(count: Int) extends Tag("Rerun")
class MyWindowsSuite extends munit.FunSuite {
  override def munitRunTest(options: TestOptions, body: => Any): Any = {
    val rerunCount = options.tags.collectFirst {
      case Rerun(n) => n
    }.getOrElse(1)
    1.to(rerunCount).map(_ => super.munitRunTest(options, body))
  }
  test("files", Rerun(10)) {
    println("Hello") // will run 10 times
  }
  test("files") {
    // will run once, like normal
  }
}
```

## Extend `munit.Suite`

Extend the base class `munit.Suite` to customize exactly what `Seq[Test]` you
want to run.

```scala
class MyCustomSuite extends munit.Suite {
  // The type returned by bodies of test cases.
  // Is defined as `Any` in `munit.FunSuite` but it's abstract in `munit.Suite`
  override type TestValue = Future[String]
  override def munitTests() = List(
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

The abstract `munit.Suite` class only includes the before/after APIs and not
other methods like `assert` or `test()`.

## Run tests in IntelliJ

MUnit test suites can be executed from in IntelliJ like normal test suites.

![Running MUnit from IntelliJ](https://i.imgur.com/oAA2ZeQ.png)

It's expected that it's not possible to run individual test cases from IntelliJ
since it does not understand the structure of the `test("name") {...}` syntax.
As a workaround, use the `.only` marker to run only a single test from IntelliJ.

```diff
- test("name") {
+ test("name".only) {
    // ...
  }
```

## Search for failed tests in large log files

Test results are formatted in a specific way to make it easy to search for them
in a large log file.

| Test    | Prefix  |
| ------- | ------- |
| Success | `+`     |
| Failed  | `==> X` |
| Ignored | `==> i` |
| Skipped | `==> s` |

Knowing these prefixes may come in handy for example when browsing test logs in
a browser. Search for "==> X" to quickly navigate to the failed tests.

## Tag flaky tests

Use `.flaky` to mark a test case that has a tendendency to fail sometimes.

```scala
  test("requests".flaky) {
    // I/O heavy tests that sometimes fail
  }
```

By default, flaky tests fail unless the `FUNSUITE_FLAKY_OK` environment variable
is set to `true`. Override the `isFlakyFailureOk` method to customize when it's
OK for flaky tests to fail.

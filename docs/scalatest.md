---
id: scalatest
title: Coming from ScalaTest
---

Add the following settings to run ScalaTest and JUnit suites with the same
testing framework as MUnit.

```scala
// build.sbt
testFrameworks := List(
  new TestFramework("munit.Framework"),
  new TestFramework("com.geirsson.junit.PantsFramework")
)
```

These settings configure all JUnit and ScalaTest suites to run with the same
testing interface as MUnit. This means that you get the same pretty-printing of
test reports for JUnit, ScalaTest and MUnit.

Next, you may want to start migrating your test suites one by one. If you only
use basic ScalaTest features, you should be able to replace usage of
`org.scalatest.FunSuite` with minimal changes like below.

```diff
- import org.scalatest.munit.AnyFunSuite
- import org.scalatest.FunSuite
+ import munit.FunSuite

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

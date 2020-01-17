package munit

class InterceptFrameworkSuite extends FunSuite {
  test("not-implemented") {
    intercept[NotImplementedError](???)
  }
  class FooException extends Exception("Foo")
  test("type-mismatch") {
    intercept[FooException](???)
  }
}

object InterceptFrameworkSuite
    extends FrameworkTest(
      classOf[InterceptFrameworkSuite],
      """|==> success munit.InterceptFrameworkSuite.not-implemented
         |==> failure munit.InterceptFrameworkSuite.type-mismatch - intercept failed, exception 'scala.NotImplementedError' is not a subtype of 'munit.InterceptFrameworkSuite.FooException
         |""".stripMargin
    )

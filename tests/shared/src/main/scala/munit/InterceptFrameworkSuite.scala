package munit

class InterceptException extends Exception("Foo")
class InterceptFrameworkSuite extends FunSuite {
  test("not-implemented") {
    intercept[NotImplementedError](???)
  }
  test("type-mismatch") {
    intercept[InterceptException](???)
  }
}

object InterceptFrameworkSuite
    extends FrameworkTest(
      classOf[InterceptFrameworkSuite],
      """|==> success munit.InterceptFrameworkSuite.not-implemented
         |==> failure munit.InterceptFrameworkSuite.type-mismatch - intercept failed, exception 'scala.NotImplementedError' is not a subtype of 'munit.InterceptException
         |""".stripMargin
    )

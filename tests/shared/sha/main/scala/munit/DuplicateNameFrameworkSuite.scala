package munit

class DuplicateNameFrameworkSuite extends FunSuite {
  test("basic") {
    // pass
  }
  test("basic") {
    // pass
  }
}

object DuplicateNameFrameworkSuite
    extends FrameworkTest(
      classOf[DuplicateNameFrameworkSuite],
      """|==> success munit.DuplicateNameFrameworkSuite.basic
         |==> success munit.DuplicateNameFrameworkSuite.basic-1
         |""".stripMargin
    )

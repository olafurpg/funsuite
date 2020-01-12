package munit

class FailFrameworkSuite extends FunSuite {
  test("pass".fail) {
    // println("pass")
  }
  test("fail".fail) {
    ???
  }
}

object FailFrameworkSuite
    extends FrameworkTest(
      classOf[FailFrameworkSuite],
      """|==> failure munit.FailFrameworkSuite.pass - /scala/munit/FailFrameworkSuite.scala:4 expected failure but test passed
         |3:class FailFrameworkSuite extends FunSuite {
         |4:  test("pass".fail) {
         |5:    // println("pass")
         |==> success munit.FailFrameworkSuite.fail
         |""".stripMargin
    )

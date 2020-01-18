package munit

import java.util
import java.util.Collections

class MyersSuite extends FunSuite {
  test("basic") {
    val myers = new difflib.myers.MyersDiff[String]()
    val path = myers.buildPath(
      util.Arrays.asList("a"),
      util.Arrays.asList("b")
    )
    Printers.log(path)

  }
}
class DiffsSuite extends FunSuite { self =>
//  test("ansi") {
//    val diff1 = Diffs.unifiedDiff("a", "b")
//    val diff2 = Diffs.unifiedDiff("a", "c")
//    val obtained = Diffs.unifiedDiff(diff1, diff2)
//    // Asserts that a roundtrip of ANSI color processing still produces
//    // intuitive results.
//    assertNoDiff(
//      obtained,
//      """|-a
//         |-+b
//         |++c
//         |""".stripMargin
//    )
//  }

  def check(
      name: String,
      a: String,
      b: String,
      expected: String
  )(implicit loc: Location): Unit = {
    test(name) {
      val obtained = Diffs.unifiedDiff(a, b)
      assertNoDiff(obtained, expected)
    }
  }

  check(
    "trailing-whitespace",
    "a\nb",
    "a \nb",
    """|-a
       |+a ∙
       | b
       |""".stripMargin
  )

//  check(
//    "windows-crlf",
//    "a\r\nb",
//    "a\nb",
//    ""
//  )

}

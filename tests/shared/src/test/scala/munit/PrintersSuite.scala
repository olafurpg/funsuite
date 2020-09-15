package munit

import munit.internal.console.Printers

class PrintersSuite extends FunSuite { self =>
  val isScala213: Boolean = BuildInfo.scalaVersion.startsWith("2.13")
  def check(
      options: TestOptions,
      original: Any,
      expected: String,
      isEnabled: Boolean = true
  ): Unit = {
    test(options) {
      assume(isEnabled, "disabled test")
      val obtained = Printers.print(original)
      assertNoDiff(obtained, expected)
    }
  }

  check(
    "basic".tag(NoDottyJS),
    "a",
    "\"a\""
  )

  check(
    "multiline",
    "a\n",
    "\"\"\"a\n\"\"\""
  )
  check(
    "single-quote",
    '\'',
    "'\\''"
  )
  check(
    "newline",
    '\n',
    "'\\n'"
  )
  check(
    "map",
    Map(1 -> 2, 3 -> 4, 5 -> Map(6 -> 7)),
    """|Map(
       |  1 -> 2,
       |  3 -> 4,
       |  5 -> Map(
       |    6 -> 7
       |  )
       |)
       |""".stripMargin
  )

  check(
    "list",
    List(1, 2, 3, List(4, 5, List(6, 7))),
    """|List(
       |  1,
       |  2,
       |  3,
       |  List(
       |    4,
       |    5,
       |    List(
       |      6,
       |      7
       |    )
       |  )
       |)
       |""".stripMargin
  )

  check(
    "array",
    Array(1, 2, 3, Array(4, 5, Array(6, 7))),
    """|Array(
       |  1,
       |  2,
       |  3,
       |  Array(
       |    4,
       |    5,
       |    Array(
       |      6,
       |      7
       |    )
       |  )
       |)
       |""".stripMargin
  )

  case class User(
      name: String,
      age: Int,
      awesome: Boolean,
      friends: List[User] = Nil
  )
  check(
    "user1",
    User("John", 42, true, Nil),
    """|User(
       |  name = "John",
       |  age = 42,
       |  awesome = true,
       |  friends = Nil
       |)
       |""".stripMargin,
    isScala213
  )

  check(
    "user2",
    User("John", 42, true, List(User("Susan", 43, true))),
    """|User(
       |  name = "John",
       |  age = 42,
       |  awesome = true,
       |  friends = List(
       |    User(
       |      name = "Susan",
       |      age = 43,
       |      awesome = true,
       |      friends = Nil
       |    )
       |  )
       |)
       |""".stripMargin,
    isScala213
  )
}

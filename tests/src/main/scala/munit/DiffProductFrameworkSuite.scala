package munit

import scala.util.Properties

class DiffProductFrameworkSuite extends FunSuite {

  case class User(name: String, age: Int, friends: List[Int])
  test("pass") {
    val john = User("John", 42, 1.to(2).toList)
    val john2 = User("John", 43, 2.to(2).toList)
    assertEqual(john2, john)
  }

}

object DiffProductFrameworkSuite
    extends FrameworkTest(
      classOf[DiffProductFrameworkSuite],
      """|==> failure munit.DiffProductFrameworkSuite.pass - /scala/munit/DiffProductFrameworkSuite.scala:11 values are not the same
         |=> Obtained
         |User(
         |  name = "John",
         |  age = 43,
         |  friends = List(
         |    2
         |  )
         |)
         |=> Diff (- obtained, + expected)
         |   name = "John",
         |-  age = 43,
         |+  age = 42,
         |   friends = List(
         |+    1,
         |     2
         |10:    val john2 = User("John", 43, 2.to(2).toList)
         |11:    assertEqual(john2, john)
         |12:  }
         |""".stripMargin
    )

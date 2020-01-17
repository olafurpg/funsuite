package munit

// Framework tests needs to be manually added to FrameworkSuite.tests
class FrameworkTest(
    val cls: Class[_ <: FunSuite],
    val expected: String
)(implicit val location: Location)

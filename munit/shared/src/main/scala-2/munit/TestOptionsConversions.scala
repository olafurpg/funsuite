package munit

trait TestOptionsConversions {

  /**
   * Implicitly create a TestOptions given a test name.
   * This allows writing `test("name") { ... }` even if `test` accepts a `TestOptions`
   */
  implicit def testOptionsFromString(
      name: String
  )(implicit loc: Location): TestOptions =
    new TestOptions(name, Set.empty, loc)
}

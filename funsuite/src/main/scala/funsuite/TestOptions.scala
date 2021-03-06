package funsuite

/**
  * Options used when running a test. It can be built implicitly from a [[String]]
  * (@see [[tests.TestOptionsConverstions]])
  *
  * @param name the test name, used in the UI and to select it with testOnly
  * @param tags a set of [[tests.Tag]], used to attach semantic information to a test
  */
case class TestOptions(name: String, tags: Set[Tag], loc: Location) {
  def fail: TestOptions = tag(Fail)
  def flaky: TestOptions = tag(Flaky)
  def ignore: TestOptions = tag(Ignore)
  def only: TestOptions = tag(Only)
  def tag(t: Tag): TestOptions = copy(tags = tags + t)
}

trait TestOptionsConversions {

  /**
    * Implicitly create a TestOptions given a test name.
    * This allows writing `test("name") { ... }` even if `test` accepts a `TestOptions`
    */
  implicit def testOptionsFromString(
      name: String
  )(implicit loc: Location): TestOptions =
    TestOptions(name, Set.empty, loc)
}

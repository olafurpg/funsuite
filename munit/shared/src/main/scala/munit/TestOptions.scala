package munit

/**
  * Options used when running a test. It can be built implicitly from a [[String]]
  * (@see [[tests.TestOptionsConverstions]])
  *
  * @param name the test name, used in the UI and to select it with testOnly
  * @param tags a set of [[tests.Tag]], used to attach semantic information to a test
  */
final class TestOptions(
    val name: String,
    val tags: Set[Tag],
    val loc: Location
) extends Serializable {
  def fail: TestOptions = tag(Fail)
  def flaky: TestOptions = tag(Flaky)
  def ignore: TestOptions = tag(Ignore)
  def only: TestOptions = tag(Only)
  def tag(t: Tag): TestOptions = copy(tags = tags + t)
  private[this] def copy(
      name: String = this.name,
      tags: Set[Tag] = this.tags,
      loc: Location = this.loc
  ): TestOptions = {
    new TestOptions(name, tags, loc)
  }
}

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

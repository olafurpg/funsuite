package munit

/**
 * Options used when running a test. It can be built implicitly from a [[String]]
 * (@see [[munit.TestOptionsConversions]])
 *
 * @param name the test name, used in the UI and to select it with testOnly
 * @param tags a set of [[munit.Tag]], used to attach semantic information to a test
 */
final class TestOptions(
    val name: String,
    val tags: Set[Tag],
    val location: Location
) extends Serializable {
  def this(name: String)(implicit loc: munit.Location) =
    this(name, Set.empty, loc)

  def withName(newName: String): TestOptions =
    copy(name = newName)
  def withTags(newTags: Set[Tag]): TestOptions =
    copy(tags = newTags)
  def withLocation(newLocation: Location): TestOptions =
    copy(location = newLocation)

  def fail: TestOptions = tag(Fail)
  def flaky: TestOptions = tag(Flaky)
  def ignore: TestOptions = tag(Ignore)
  def only: TestOptions = tag(Only)
  def tag(t: Tag): TestOptions = copy(tags = tags + t)
  private[this] def copy(
      name: String = this.name,
      tags: Set[Tag] = this.tags,
      location: Location = this.location
  ): TestOptions = {
    new TestOptions(name, tags, location)
  }

  override def toString: String =
    s"TestOptions($name, $tags, $location)"
}

object TestOptions extends TestOptionsConversions {
  def apply(name: String)(implicit loc: munit.Location): TestOptions =
    new TestOptions(name)
}

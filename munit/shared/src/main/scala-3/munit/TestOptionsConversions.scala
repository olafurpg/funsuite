package munit

trait TestOptionsConversions {

  extension (s: String)(using loc: munit.Location) {
    private def o = new TestOptions(s)
    def withName(newName: String): TestOptions =
      o.withName(newName)
    def withTags(newTags: Set[Tag]): TestOptions =
      o.withTags(newTags)
    def withLocation(newLocation: Location): TestOptions =
      o.withLocation(newLocation)

    def fail: TestOptions = o.fail
    def flaky: TestOptions = o.flaky
    def ignore: TestOptions = o.ignore
    def only: TestOptions = o.only
    def tag(t: Tag): TestOptions = o.tag(t)
  }

  implicit def testOptionsFromString(
      name: String
  )(implicit loc: Location): TestOptions =
    new TestOptions(name, Set.empty, loc)

}

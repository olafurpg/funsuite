package munit

import munit.internal.PlatformCompat
import munit.internal.console.Lines
import java.nio.file.Paths

class BaseSuite extends FunSuite {

  override def munitTestTransforms: List[TestTransform] =
    super.munitTestTransforms ++ List(
      new TestTransform(
        "BaseSuite",
        { test =>
          def isDotty: Boolean =
            BuildInfo.scalaVersion.startsWith("0.")
          def is213: Boolean =
            BuildInfo.scalaVersion.startsWith("2.13") || isDotty
          if (test.tags(NoDotty) && isDotty) {
            test.tag(Ignore)
          } else if (test.tags(Only213) && !is213) {
            test.tag(Ignore)
          } else if (test.tags(OnlyJVM) && !PlatformCompat.isJVM) {
            test.tag(Ignore)
          } else {
            test
          }
        }
      )
    )
}

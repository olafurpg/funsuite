package munit

import munit.internal.PlatformCompat
import scala.concurrent.Future

class BaseSuite extends FunSuite {
  override def munitRunTest(
      options: TestOptions,
      body: => Future[Any]
  ): Future[Any] = {
    def isDotty: Boolean =
      BuildInfo.scalaVersion.startsWith("0.")
    def is213: Boolean =
      BuildInfo.scalaVersion.startsWith("2.13") || isDotty
    if (options.tags(NoDotty) && isDotty) {
      Future.successful(Ignore)
    } else if (options.tags(Only213) && !is213) {
      Future.successful(Ignore)
    } else if (options.tags(OnlyJVM) && !PlatformCompat.isJVM) {
      Future.successful(Ignore)
    } else {
      super.munitRunTest(options, body)
    }
  }
}

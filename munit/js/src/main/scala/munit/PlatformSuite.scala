package munit

import munit.internal.MUnitBootstrapper
import org.scalajs.junit.Bootstrapper
import scala.scalajs.reflect.annotation._

@EnableReflectiveInstantiation
trait PlatformSuite { self: Suite =>
  // object $scalajs$junit$bootstrapper extends MUnitBootstrapper(self)
}

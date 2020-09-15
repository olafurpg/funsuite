package munit

import scala.collection.mutable
import org.scalacheck.Prop.forAll

final class ScalaCheckInitialSeedSuite extends ScalaCheckSuite {

  // initial seed should be used for the first out of 100 `minSuccessfulTests` only
  override val scalaCheckInitialSeed =
    "9SohH7wEYXCdXK4b9yM2d6TKIN2jBFcBs4QBta-2yTD="
  private val ints = mutable.Set.empty[Int]

  property("generating int") {
    forAll { (i: Int) =>
      ints.add(i)
      true
    }
  }

  test("generated int are not all the same".tag(NoDottyJS)) {
    assert(ints.size > 1)
  }
}

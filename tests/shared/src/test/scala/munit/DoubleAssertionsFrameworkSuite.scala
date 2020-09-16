package munit

class DoubleAssertionsFrameworkSuite extends BaseSuite {
  test("Assert Equals NaN Fails".fail) {
    assertEqualsDouble(1.234, Double.NaN, 0.0)
  }

  test("Assert NaN Equals Fails".fail) {
    assertEqualsDouble(Double.NaN, 1.234, 0.0)
  }

  test("Assert NaN Equals NaN") {
    assertEqualsDouble(Double.NaN, Double.NaN, 0.0)
  }

  test("Assert Pos Infinity Not Equals Neg Infinity".fail) {
    assertEqualsDouble(Double.PositiveInfinity, Double.NegativeInfinity, 0.0)
  }

  test("Assert Pos Infinity Not Equals".fail) {
    assertEqualsDouble(Double.PositiveInfinity, 1.23, 0.0)
  }

  test("Assert Pos Infinity Equals Infinity") {
    assertEqualsDouble(Double.PositiveInfinity, Double.PositiveInfinity, 0.0)
  }

  test("Assert Neg Infinity Equals Infinity") {
    assertEqualsDouble(Double.NegativeInfinity, Double.NegativeInfinity, 0.0)
  }

  test("All Infinities") {
    assertEqualsDouble(
      Double.PositiveInfinity,
      Double.NegativeInfinity,
      Double.PositiveInfinity
    )
  }

  // And now, the same with floats...
  test("Assert Equals NaN Fails".fail) {
    assertEqualsFloat(1.234f, Float.NaN, 0.0f)
  }

  test("Assert NaN Equals Fails".fail) {
    assertEqualsFloat(Float.NaN, 1.234f, 0.0f)
  }

  test("Assert NaN Equals NaN") {
    assertEqualsFloat(Float.NaN, Float.NaN, 0.0f)
  }

  test("Assert Pos Infinity Not Equals Neg Infinity".fail) {
    assertEqualsFloat(Float.PositiveInfinity, Float.NegativeInfinity, 0.0f)
  }

  test("Assert Pos Infinity Not Equals".fail) {
    assertEqualsFloat(Float.PositiveInfinity, 1.23f, 0.0f)
  }

  test("Assert Pos Infinity Equals Infinity") {
    assertEqualsFloat(Float.PositiveInfinity, Float.PositiveInfinity, 0.0f)
  }

  test("Assert Neg Infinity Equals Infinity") {
    assertEqualsFloat(Float.NegativeInfinity, Float.NegativeInfinity, 0.0f)
  }

  test("All Infinities") {
    assertEqualsFloat(
      Float.PositiveInfinity,
      Float.NegativeInfinity,
      Float.PositiveInfinity
    )
  }
}

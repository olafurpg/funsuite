package munit

import org.junit.runner.RunWith

/** The base class for all test suites.
  * Extend this class if you don't need the functionality in FunSuite.
  */
@RunWith(classOf[MUnitRunner])
abstract class Suite {

  /** The value produced by test bodies. */
  type TestValue
  final type Test = GenericTest[TestValue]
  final type BeforeEach = GenericBeforeEach[TestValue]
  final type AfterEach = GenericAfterEach[TestValue]

  /** The base class for all test suites */
  def munitTests(): Seq[Test]

  /** Runs once before all test cases.
    * An error in this method aborts the test suite.
    */
  def beforeAll(): Unit = ()

  /** Runs once after all test cases. */
  def afterAll(): Unit = ()

  /** Runs before each individual test case.
    * An error in this method aborts the test case.
    */
  def beforeEach(context: BeforeEach): Unit = ()

  /** Runs after each individual test case. */
  def afterEach(context: AfterEach): Unit = ()

}

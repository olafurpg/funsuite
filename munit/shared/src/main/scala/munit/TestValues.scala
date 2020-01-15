package munit

import scala.util.control.NoStackTrace

/**
  * Values that have special treatment when evaluating values produced by tests.
  */
object TestValues {

  /** The test failed with the given exception but was ignored but its marked as flaky */
  class FlakyFailure(error: Throwable)
      extends FailException(
        "ignoring flaky test failure",
        error,
        Location.empty
      )
      with NoStackTrace
      with Serializable

  /** The test case was ignored. */
  val Ignore = munit.Ignore
}

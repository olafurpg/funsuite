package difflib

import java.{util => ju}

object DiffUtils {
  def generateUnifiedDiff(
      obtainedTitle: String,
      expectedTitle: String,
      original: ju.List[String],
      patch: Patch[String],
      context: Int
  ): ju.List[String] = ???
  def diff(
      original: ju.List[String],
      revised: ju.List[String]
  ): Patch[String] =
    ???
  private def diff(
      original: ju.List[String],
      revised: ju.List[String],
      algorithm: DiffAlgorithm[String]
  ): Patch[String] = {
    algorithm.diff(original, revised)
  }
}

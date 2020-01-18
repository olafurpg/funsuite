package difflib.myers

import java.util

import difflib.{DiffAlgorithm, Patch}

class MyersDiff(equalizer: Equalizer[String]) extends DiffAlgorithm[String] {
  def this() = this(Equalizer.default[String])
  override def diff(
      original: util.List[String],
      revised: util.List[String]
  ): Patch[String] = {
    try {
      buildRevision(buildPath(original, revised), original, revised)
    } catch {
      case e: DifferentiationFailedException =>
        e.printStackTrace()
        new Patch[String]()
    }
  }
  private def buildRevision(
      path: PathNode,
      original: util.List[String],
      revised: util.List[String]
  ): Patch[String] = {
    ???
  }

  private def buildPath(
      original: util.List[String],
      revised: util.List[String]
  ): PathNode = {
    ???
  }
}

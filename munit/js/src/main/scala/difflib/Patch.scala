package difflib

import java.{util => ju}

class Patch[T] {
  def getDeltas(): ju.List[Delta[T]] = ???
}

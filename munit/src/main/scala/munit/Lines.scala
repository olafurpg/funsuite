package munit

import scala.collection.mutable
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files
import scala.util.control.NonFatal
import scala.collection.JavaConverters._

class Lines extends Serializable {
  private val filecache = mutable.Map.empty[Path, Array[String]]

  def formatLine(location: Location, message: String): String = {
    try {
      val path = Paths.get(location.path)
      val lines = filecache.getOrElseUpdate(path, {
        Files.readAllLines(path).asScala.toArray
      })
      val slice = lines.slice(location.line - 2, location.line + 1)
      val out = new StringBuilder()
      if (slice.length == 3) {
        val width = (location.line + 1).toString().length()
        def format(n: Int): String = {
          val number = n.toString() + ":"
          val padding = " " * (width - number.length() + 1)
          number + padding
        }
        out
          .append(location.path)
          .append(':')
          .append(location.line.toString())
          .append(if (message.length == 0) "" else " ")
          .append(message)
          .append('\n')
          .append(format(location.line - 1))
          .append(slice(0))
          .append('\n')
          .append(AnsiColors.Reversed)
          .append(format(location.line))
          .append(slice(1))
          .append(AnsiColors.Reset)
          .append('\n')
          .append(format(location.line + 1))
          .append(slice(2))
      }
      out.toString()
    } catch {
      case NonFatal(_) =>
        ""
    }
  }
}

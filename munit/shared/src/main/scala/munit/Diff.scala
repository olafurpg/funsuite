package munit

import com.geirsson.junit.Ansi
import collection.JavaConverters._

class Diff(val obtained: String, val expected: String) extends Serializable {
  val obtainedClean = Ansi.filterAnsi(obtained)
  val expectedClean = Ansi.filterAnsi(expected)
  val obtainedLines = splitIntoLines(obtainedClean)
  val expectedLines = splitIntoLines(expectedClean)
  val unifiedDiff = createUnifiedDiff(obtainedLines, expectedLines)
  def isEmpty: Boolean = unifiedDiff.isEmpty()

  def createReport(
      title: String,
      printObtainedAsStripMargin: Boolean = true
  ): String = {
    val sb = new StringBuilder
    if (title.nonEmpty) {
      sb.append(title)
        .append("\n")
    }
    if (obtainedClean.length < 1000) {
      header("Obtained", sb).append("\n")
      if (printObtainedAsStripMargin) {
        sb.append(asStripMargin(obtainedClean))
      } else {
        sb.append(obtainedClean)
      }
      sb.append("\n")
    }
    appendDiffOnlyReport(sb)
    sb.toString()
  }

  def createDiffOnlyReport(): String = {
    val out = new StringBuilder
    appendDiffOnlyReport(out)
    out.toString()
  }

  private def appendDiffOnlyReport(sb: StringBuilder): Unit = {
    header("Diff", sb)
    sb.append(
        s" (${AnsiColors.LightRed}- obtained${AnsiColors.Reset}, ${AnsiColors.LightGreen}+ expected${AnsiColors.Reset})"
      )
      .append("\n")
    sb.append(unifiedDiff)
  }

  private def asStripMargin(obtained: String): String = {
    if (!obtained.contains("\n")) Printers.print(obtained)
    else {
      val out = new StringBuilder
      val lines = obtained.trim.linesIterator
      out.append("    \"\"\"|" + lines.next() + "\n")
      lines.foreach(line => {
        out.append("       |").append(line).append("\n")
      })
      out.append("       |\"\"\".stripMargin")
      out.toString()
    }
  }

  private def header(t: String, sb: StringBuilder): StringBuilder = {
    sb.append(AnsiColors(s"=> $t", AnsiColors.Bold))
  }

  private def createUnifiedDiff(
      original: Seq[String],
      revised: Seq[String]
  ): String = {
    val diff = difflib.DiffUtils.diff(original.asJava, revised.asJava)
    Printers.log(diff)
    val result =
      if (diff.getDeltas.isEmpty) ""
      else {
        difflib.DiffUtils
          .generateUnifiedDiff(
            "obtained",
            "expected",
            original.asJava,
            diff,
            1
          )
          .asScala
          .iterator
          .drop(2)
          .filterNot(_.startsWith("@@"))
          .map { line =>
            if (line.isEmpty()) line
            else if (line.last == ' ') line + "∙"
            else line
          }
          .map { line =>
            if (line.startsWith("-")) AnsiColors(line, AnsiColors.LightRed)
            else if (line.startsWith("+"))
              AnsiColors(line, AnsiColors.LightGreen)
            else line
          }
          .mkString("\n")
      }
    result
  }

  private def splitIntoLines(string: String): Seq[String] = {
    string.trim().replaceAllLiterally("\r\n", "\n").split("\n").toIndexedSeq
  }
}

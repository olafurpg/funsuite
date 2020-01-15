// Adaptation of https://github.com/lihaoyi/PPrint/blob/e6a918c259ed7ae1998bbf58c360334a3f0157ca/pprint/src/pprint/Walker.scala
package munit

import scala.annotation.switch
import munit.internal.Compat
import com.geirsson.junit.Ansi

object Printers {
  def log(any: Any, printer: Printer = EmptyPrinter)(
      implicit loc: Location
  ): Unit = {
    println(loc.toString)
    println(print(any, printer))
  }

  /** Pretty-prints the value in a format that's optimized for producing diffs */
  def print(any: Any, printer: Printer = EmptyPrinter): String = {
    var height = printer.height
    val out = new StringBuilder()
    val indentStep = 2
    def loop(a: Any, indent: Int): Unit = {
      height -= 1
      if (height < 0) {
        out.append("...")
        return
      }
      val nextIndent = indent + indentStep
      val isDone = printer.print(a, out, indent)
      if (!isDone) {
        a match {
          case null         => out.append("null")
          case x: Printable => x.print(out, indent)
          case x: Char =>
            out.append('\'')
            printChar(x, out)
            out.append('\'')
          case x: Byte   => out.append(x.toString())
          case x: Short  => out.append(x.toString())
          case x: Int    => out.append(x.toString())
          case x: Long   => out.append(x.toString())
          case x: Float  => out.append(x.toString())
          case x: Double => out.append(x.toString())
          case x: String => printString(x, out, printer)
          case None =>
            out.append("None")
          case Nil =>
            out.append("Nil")
          case x: Map[_, _] =>
            printApply(
              Compat.collectionClassName(x),
              x.iterator,
              out,
              indent,
              nextIndent
            ) {
              case (key, value) =>
                loop(key, nextIndent)
                out.append(" -> ")
                loop(value, nextIndent)
            }
          case x: Iterable[_] =>
            printApply(
              Compat.collectionClassName(x),
              x.iterator,
              out,
              indent,
              nextIndent
            ) { value =>
              loop(value, nextIndent)
            }
          case x: Array[_] =>
            printApply(
              "Array",
              x.iterator,
              out,
              indent,
              nextIndent
            ) { value =>
              loop(value, nextIndent)
            }
          case it: Iterator[_] =>
            if (it.isEmpty) out.append("empty iterator")
            else out.append("non-empty iterator")
          case p: Product =>
            val elementNames = Compat.productElementNames(p)
            val infiniteElementNames = Iterator.continually {
              if (elementNames.hasNext) elementNames.next()
              else ""
            }
            printApply(
              p.productPrefix,
              p.productIterator.zip(infiniteElementNames),
              out,
              indent,
              nextIndent
            ) {
              case (value, key) =>
                if (key.nonEmpty) {
                  out.append(key).append(" = ")
                }
                loop(value, nextIndent)
            }
          case _ =>
            out.append(any.toString())
        }
      }
    }
    loop(any, indent = 0)
    Ansi.filterAnsi(out.toString())
  }

  private def printApply[T](
      prefix: String,
      it: Iterator[T],
      out: StringBuilder,
      indent: Int,
      nextIndent: Int
  )(fn: T => Unit): Unit = {
    out.append(prefix)
    out.append('(')
    if (it.hasNext) {
      printNewline(out, nextIndent)
      while (it.hasNext) {
        val value = it.next()
        fn(value)
        if (it.hasNext) {
          out.append(',')
          printNewline(out, nextIndent)
        } else {
          printNewline(out, indent)
        }
      }
    }
    out.append(')')
  }

  private def printNewline(out: StringBuilder, indent: Int): Unit = {
    out.append("\n")
    var i = 0
    while (i < indent) {
      out.append(' ')
      i += 1
    }
  }

  private def printString(
      string: String,
      out: StringBuilder,
      printer: Printer
  ): Unit = {
    val isMultiline = printer.isMultiline(string)
    if (isMultiline) {
      out.append('"')
      out.append('"')
      out.append('"')
      out.append(string)
      out.append('"')
      out.append('"')
      out.append('"')
    } else {
      out.append('"')
      var i = 0
      while (i < string.length()) {
        printChar(string.charAt(i), out)
        i += 1
      }
      out.append('"')
    }
  }

  private def printChar(c: Char, sb: StringBuilder) =
    (c: @switch) match {
      case '"'  => sb.append("\\\"")
      case '\'' => sb.append("\\'")
      case '\\' => sb.append("\\\\")
      case '\b' => sb.append("\\b")
      case '\f' => sb.append("\\f")
      case '\n' => sb.append("\\n")
      case '\r' => sb.append("\\r")
      case '\t' => sb.append("\\t")
      case c =>
        val isUnicode = false
        if (c < ' ' || (c > '~' && isUnicode))
          sb.append("\\u%04x" format c.toInt)
        else sb.append(c)
    }

}

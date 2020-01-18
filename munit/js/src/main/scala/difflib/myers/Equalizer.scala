package difflib.myers

import munit.Printers

trait Equalizer[T] {
  def equals(original: T, revised: T): Boolean
}
object Equalizer {
  def default[T]: Equalizer[T] = new Equalizer[T] {
    override def equals(original: T, revised: T): Boolean = {
      println(s"orig: ${Printers.print(original)}")
      println(s"rev: ${Printers.print(revised)}")
      original.equals(revised)
    }
  }
}

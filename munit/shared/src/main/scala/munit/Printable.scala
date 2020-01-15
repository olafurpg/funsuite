package munit

/**
  * Override this class to customize the default pretty-printer.
  */
trait Printable {
  def print(out: StringBuilder, indent: Int): Unit
}

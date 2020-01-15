package munit

/**
  * Implement this trait to customize the default printer
  */
trait Printer {

  /**
    * Pretty-print a single value during pretty printing.
    *
    * Returns true if this value has been printed, false if FunSuite should fallback to the default pretty-printer.
    */
  def print(value: Any, out: StringBuilder, indent: Int): Boolean
  def height: Int = 100
  def isMultiline(string: String): Boolean =
    string.contains('\n')
}

/** Default printer that does not customize the pretty-printer */
object EmptyPrinter extends Printer {
  def print(value: Any, out: StringBuilder, indent: Int): Boolean = false
}

package munit

object AnsiColors {
  val LightRed = "\u001b[91m"
  val LightGreen = "\u001b[92m"
  val Reset = "\u001b[0m"
  val Reversed = "\u001b[7m"
  val Bold = "\u001b[1m"
  def apply(string: String, color: String): String =
    color + string + Reset
}

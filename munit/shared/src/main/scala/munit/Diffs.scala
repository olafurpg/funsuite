package munit

object Diffs {

  def create(obtained: String, expected: String): Diff =
    new Diff(obtained, expected)

  def assertNoDiff(
      obtained: String,
      expected: String,
      title: String = "",
      printObtainedAsStripMargin: Boolean = true
  )(implicit loc: Location): Boolean = {
    if (obtained.isEmpty && !expected.isEmpty) {
      Assertions.fail("Obtained empty output!")
    }
    val diff = new Diff(obtained, expected)
    Printers.log(diff.unifiedDiff)
    if (diff.isEmpty) true
    else {
      Assertions.fail(diff.createReport(title, printObtainedAsStripMargin))
    }
  }

  def createDiffOnlyReport(
      obtained: String,
      expected: String
  ): String = {
    create(obtained, expected).createDiffOnlyReport()
  }

  def createReport(
      obtained: String,
      expected: String,
      title: String,
      printObtainedAsStripMargin: Boolean = true
  ): String = {
    create(obtained, expected).createReport(title, printObtainedAsStripMargin)
  }

  def unifiedDiff(obtained: String, expected: String): String = {
    create(obtained, expected).unifiedDiff
  }

}

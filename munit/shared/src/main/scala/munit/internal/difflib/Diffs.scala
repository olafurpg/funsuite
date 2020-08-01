package munit.internal.difflib

import munit.Location

object Diffs {

  def create(obtained: String, expected: String): Diff =
    new Diff(obtained, expected, isTrimmed = true)

  def assertNoDiff(
      obtained: String,
      expected: String,
      fail: String => Nothing,
      title: String = "",
      printObtainedAsStripMargin: Boolean = true,
      isTrimmed: Boolean = true
  )(implicit loc: Location): Boolean = {
    if (obtained.isEmpty && !expected.isEmpty) {
      fail("Obtained empty output!")
    }
    val diff = new Diff(obtained, expected, isTrimmed)
    if (diff.isEmpty) true
    else {
      fail(diff.createReport(title, printObtainedAsStripMargin))
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

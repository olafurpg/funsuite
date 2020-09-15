package munit.internal.junitinterface

import org.junit.runner.Description
import org.junit.runner.manipulation.Filter

class TagsFilter(
    val include: Set[String],
    val exclude: Set[String]
) extends Filter {
  override def shouldRun(description: Description): Boolean = {
    if (include.isEmpty && exclude.isEmpty) true
    else {
      var isIncluded = include.isEmpty
      var isExcluded = false
      description.getAnnotations.foreach {
        case t: Tag =>
          isIncluded ||= include.contains(t.value)
          isExcluded ||= exclude.contains(t.value)
        case _ =>
      }
      isIncluded && !isExcluded
    }
  }
}

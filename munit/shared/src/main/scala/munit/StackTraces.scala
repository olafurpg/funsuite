// Adaptation of https://github.com/lihaoyi/utest/blob/5c0fd1c25381592e1301f4bd97b0bf18a65a0a1a/utest/src/utest/framework/StackMarker.scala
package munit

import java.{util => ju}

/** Utility methods for trimming unrelated elements from stack traces */
class StackTraces
object StackTraces {
  @noinline
  def dropInside[T](t: => T): T = t
  @noinline
  def dropOutside[T](t: => T): T = t

  def trimStackTrace(ex: Throwable): ex.type = {
    val isVisited = ju.Collections.newSetFromMap(
      new ju.IdentityHashMap[Throwable, java.lang.Boolean]()
    )
    def loop(e: Throwable): Unit = {
      if (e != null && isVisited.add(e)) {
        val stack = e.getStackTrace()
        if (stack != null) {
//          e.setStackTrace(filterCallStack(stack))
        }
        loop(e.getCause())
      }
    }
    loop(ex)
    ex
  }
  private val className = "munit.StackTraces$"
  def filterCallStack(
      stack: Array[StackTraceElement]
  ): Array[StackTraceElement] = {
    val droppedInside = stack.lastIndexWhere(x =>
      x.getClassName == className &&
        x.getMethodName == "dropInside"
    )

    val droppedOutside = stack.indexWhere(x =>
      x.getClassName == className &&
        x.getMethodName == "dropOutside"
    )

    val stack1 = stack.slice(
      droppedInside match {
        case -1 => 0
        case n  => n + 3
      },
      droppedOutside match {
        case -1 => stack.length
        case n  => n
      }
    )

    val lastNonLMFIndex =
      stack1.lastIndexWhere(x => !x.getClassName.contains("$$Lambda$"))

    if (lastNonLMFIndex < 0) stack1
    else stack1.take(lastNonLMFIndex + 1)
  }
}

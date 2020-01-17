package munit.internal

import sbt.testing.Event
import sbt.testing.Fingerprint
import sbt.testing.Selector
import sbt.testing.Status
import sbt.testing.OptionalThrowable
import sbt.testing.Task
import sbt.testing.TaskDef
import sbt.testing.TestSelector

class SbtEvent(
    val status: Status,
    val duration: Long,
    task: TaskDef,
    ex: Option[Throwable] = None
) extends Event {
  def fullyQualifiedName(): String = task.fullyQualifiedName()
  def fingerprint(): Fingerprint = task.fingerprint()
  def selector(): Selector =
    task
      .selectors()
      .headOption
      .getOrElse(new TestSelector(task.fullyQualifiedName()))
  def throwable(): OptionalThrowable = ex match {
    case Some(value) => new OptionalThrowable(value)
    case None        => new OptionalThrowable()
  }
}

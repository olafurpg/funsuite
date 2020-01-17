package munit.internal

import sbt.testing.TaskDef
import sbt.testing.Runner
import sbt.testing.SubclassFingerprint
import sbt.testing.Task
import sbt.testing.{EventHandler, Logger}
import sbt.testing.Status
import scala.concurrent.Future

class SbtRunner(
    val remoteArgs: Array[String],
    val args: Array[String],
    testClassLoader: ClassLoader,
    send: Option[String => Unit] = None
) extends Runner {
  def receiveMessage(msg: String): Option[String] = {
    println(s"receive message: $msg")
    None
  }
  def serializeTask(task: Task, serializer: TaskDef => String): String = {
    println("serializeTask")
    ""
  }
  def deserializeTask(task: String, deserializer: String => TaskDef): Task = {
    println(s"deserializeTask: $task")
    new SbtTask(deserializer(""), (_, _) => Future.successful(()))
  }
  def done: String = ""
  def tasks(taskDefs: Array[TaskDef]): Array[Task] = {
    taskDefs.map { task =>
      task.fingerprint() match {
        case f: SubclassFingerprint if !f.isModule() =>
          new SbtTask(
            task,
            (loggers, eventHandler) => {
              println("execute")
              Future.successful(())
            }
          )
        case _ =>
          new SbtTask(
            task,
            (loggers, eventHandler) => {
              println("executeJS")
              eventHandler.handle(
                new SbtEvent(
                  Status.Error,
                  0,
                  task,
                  Some(
                    new IllegalArgumentException(
                      s"not a class that extends munit.Suite: ${task.fullyQualifiedName()}"
                    )
                  )
                )
              )
              Future.successful(())
            }
          )
      }
    }
  }
}

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
    serializer(task.taskDef())
  }
  def deserializeTask(task: String, deserializer: String => TaskDef): Task = {
    new SbtTask(deserializer(task), (loggers, eventHandler) => {
      println(s"deserialize: $task")
      Future.successful(())
    })
  }
  def done: String = ""
  def tasks(taskDefs: Array[TaskDef]): Array[Task] = {
    taskDefs.map { task =>
      task.fingerprint() match {
        case f: SubclassFingerprint if !f.isModule() =>
          new SbtTask(
            task,
            (loggers, eventHandler) => {
              println(s"execute: ${task.fullyQualifiedName()}")
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

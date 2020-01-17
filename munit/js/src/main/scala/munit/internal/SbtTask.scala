package munit.internal

import sbt.testing._
import sbt.testing

import scala.concurrent.{Await, Future}
import concurrent.duration._

class SbtTask(
    val taskDef: TaskDef,
    runTasks: (Seq[Logger], EventHandler) => Future[Unit]
) extends sbt.testing.Task {

  def tags(): Array[String] = Array()

  def execute(
      eventHandler: EventHandler,
      loggers: Array[Logger]
  ): Array[testing.Task] = {
    Await.result(runTasks(loggers, eventHandler), Duration.Inf)
    Array()
  }

  def execute(
      eventHandler: EventHandler,
      loggers: Array[Logger],
      continuation: Array[testing.Task] => Unit
  ) = {
    implicit val ec = ExecutionContext.RunNow

    runTasks(loggers, eventHandler)
      .recover {
        case t =>
          loggers.foreach(_.trace(t))
      }
      .onComplete { _ =>
        continuation(Array())
      }
  }
}

/*
 * Adapted from https://github.com/scala-js/scala-js, see NOTICE.md.
 */

package com.geirsson.junit

import munit.{MUnitRunner, Suite}
import org.junit.runner.notification.RunNotifier
import sbt.testing._

import scala.concurrent.Future
import scala.scalajs.reflect.Reflect
import scala.util.control.NonFatal

/* Implementation note: In JUnitTask we use Future[Try[Unit]] instead of simply
 * Future[Unit]. This is to prevent Scala's Future implementation to box/wrap
 * fatal errors (most importantly AssertionError) in ExecutionExceptions. We
 * need to prevent the wrapping in order to hide the fact that we use async
 * under the hood and stay consistent with JVM JUnit.
 */
final class JUnitTask(
    val taskDef: TaskDef,
    runSettings: RunSettings
) extends Task {

  def tags: Array[String] = Array.empty

  def execute(
      eventHandler: EventHandler,
      loggers: Array[Logger]
  ): Array[Task] = {
    execute(eventHandler, loggers, _ => ())
    Array()
  }

  def execute(
      eventHandler: EventHandler,
      loggers: Array[Logger],
      continuation: Array[Task] => Unit
  ): Unit = {
    Reflect.lookupInstantiatableClass(taskDef.fullyQualifiedName()) match {
      case None => Future.successful(())
      case Some(cls) =>
        val runner = new MUnitRunner(
          cls.runtimeClass.asInstanceOf[Class[_ <: Suite]],
          () => cls.newInstance().asInstanceOf[Suite]
        )
        val reporter =
          new JUnitReporter(eventHandler, loggers, runSettings, taskDef)
        val notifier: RunNotifier = new MUnitRunNotifier(reporter)
        try runner.run(notifier)
        catch {
          case NonFatal(e) =>
            reporter.reportErrors("unexpected error", None, 0L, List(e))
        }
        continuation(Array())
    }
  }

}

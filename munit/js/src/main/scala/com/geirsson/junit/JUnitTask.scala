/*
 * Scala.js (https://www.scala-js.org/)
 *
 * Copyright EPFL.
 *
 * Licensed under Apache License 2.0
 * (https://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package com.geirsson.junit

import java.util.concurrent.TimeUnit

import munit.{MUnitRunner, Suite}
import org.junit.runner.{Description, notification}
import org.junit.runner.notification.RunNotifier
import sbt.testing._

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.reflect.Reflect
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

/* Implementation note: In JUnitTask we use Future[Try[Unit]] instead of simply
 * Future[Unit]. This is to prevent Scala's Future implementation to box/wrap
 * fatal errors (most importantly AssertionError) in ExecutionExceptions. We
 * need to prevent the wrapping in order to hide the fact that we use async
 * under the hood and stay consistent with JVM JUnit.
 */
private[junit] final class JUnitTask(
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
        val reporter = new Reporter(eventHandler, loggers, runSettings, taskDef)
        val notifier = new RunNotifier {
          var ignored = 0
          var total = 0
          var startedTimestamp = 0L
          val isFailed = mutable.Set.empty[String]
          override def fireTestSuiteStarted(description: Description): Unit = ()
          override def fireTestStarted(description: Description): Unit = {
            startedTimestamp = System.nanoTime()
            reporter.reportRunStarted()
          }
          def elapsedSeconds(): Double = {
            val elapsedNanos = System.nanoTime() - startedTimestamp
            elapsedNanos / 1000000000.0
          }
          override def fireTestIgnored(description: Description): Unit = {
            ignored += 1
            reporter.reportIgnored(description.getMethodName)
          }
          override def fireTestFinished(description: Description): Unit = {
            println(s"test finished: ${description.getMethodName}")
            description.getMethodName match {
              case Some(methodName) =>
                total += 1
                reporter.reportTestFinished(
                  methodName,
                  succeeded = !isFailed(methodName),
                  elapsedSeconds()
                )
              case None =>
                println(s"missing method name: $description")
            }
          }
          override def fireTestSuiteFinished(description: Description): Unit = {
            println(s"suite finished: ${description.getMethodName}")
            reporter.reportRunFinished(
              isFailed.size,
              ignored,
              total,
              elapsedSeconds()
            )
          }

          override def fireTestFailure(failure: notification.Failure): Unit = {
            failure.description.getMethodName match {
              case Some(methodName) =>
                isFailed += methodName
                reporter.reportTestFinished(
                  methodName,
                  succeeded = false,
                  elapsedSeconds()
                )
                reporter.reportErrors(
                  failure.description.getTestClass.fold("")(_.getName),
                  failure.description.getMethodName,
                  elapsedSeconds(),
                  List(failure.ex)
                )
              case None =>
                println(s"missing method name: ${failure.description}")
            }
          }
          override def fireTestAssumptionFailed(
              failure: notification.Failure
          ): Unit = {
            failure.description.getMethodName match {
              case Some(methodName) =>
                reporter.reportAssumptionViolation(
                  methodName,
                  elapsedSeconds(),
                  failure.ex
                )
              case None =>
                println(s"missing method name: ${failure.description}")
            }
          }
        }
        try runner.run(notifier)
        catch {
          case NonFatal(e) =>
            reporter.reportErrors("unexpected error", None, 0L, List(e))
        }
        continuation(Array())
    }
  }

}

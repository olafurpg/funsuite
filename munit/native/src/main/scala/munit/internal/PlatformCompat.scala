package munit.internal

import sbt.testing.TaskDef
import munit.MUnitRunner
import scala.concurrent.Future
import scala.scalanative.testinterface.PreloadedClassLoader
import sbt.testing.Task
import sbt.testing.EventHandler
import sbt.testing.Logger

object PlatformCompat {
  def executeAsync(
      task: Task,
      eventHandler: EventHandler,
      loggers: Array[Logger]
  ): Future[Unit] = {
    task.execute(eventHandler, loggers)
    Future.successful(())
  }
  // Scala Native does not support looking up annotations at runtime.
  def isIgnoreSuite(cls: Class[_]): Boolean = false

  def isJVM: Boolean = false
  def isJS: Boolean = false
  def isNative: Boolean = true

  def newRunner(
      taskDef: TaskDef,
      classLoader: ClassLoader
  ): Option[MUnitRunner] = {
    scala.util.Try {
      val suite = classLoader
        .asInstanceOf[PreloadedClassLoader]
        .loadPreloaded(taskDef.fullyQualifiedName)
        .asInstanceOf[munit.Suite]
      new MUnitRunner(suite.getClass, () => suite)
    }.toOption
  }
  private var myClassLoader: ClassLoader = _
  def setThisClassLoader(loader: ClassLoader): Unit = myClassLoader = loader
  def getThisClassLoader: ClassLoader = myClassLoader
}

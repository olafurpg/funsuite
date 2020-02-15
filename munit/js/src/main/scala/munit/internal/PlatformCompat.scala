package munit.internal

import scala.scalajs.reflect.Reflect
import sbt.testing.TaskDef
import munit.MUnitRunner
import scala.concurrent.Future
import sbt.testing.Task
import sbt.testing.EventHandler
import sbt.testing.Logger
import scala.concurrent.Promise
import scala.util.Success
import scala.concurrent.duration.Duration

object PlatformCompat {
  def executeAsync(
      task: Task,
      eventHandler: EventHandler,
      loggers: Array[Logger]
  ): Future[Unit] = {
    val p = Promise[Unit]()
    task.execute(eventHandler, loggers, _ => p.success(()))
    p.future
  }
  def await(future: Future[_], duration: Duration): Any = {
    future.value match {
      case Some(Success(value)) => value
      case _                    => future
    }
  }
  // Scala.js does not support looking up annotations at runtime.
  def isIgnoreSuite(cls: Class[_]): Boolean = false

  def isJVM: Boolean = false
  def isJS: Boolean = true
  def isNative: Boolean = false

  def newRunner(
      taskDef: TaskDef,
      classLoader: ClassLoader
  ): Option[MUnitRunner] = {
    Reflect
      .lookupInstantiatableClass(taskDef.fullyQualifiedName())
      .map(cls =>
        new MUnitRunner(
          cls.runtimeClass.asInstanceOf[Class[_ <: munit.Suite]],
          () => cls.newInstance().asInstanceOf[munit.Suite]
        )
      )
  }
  private var myClassLoader: ClassLoader = _
  def setThisClassLoader(loader: ClassLoader): Unit = myClassLoader = loader
  def getThisClassLoader: ClassLoader = myClassLoader
}

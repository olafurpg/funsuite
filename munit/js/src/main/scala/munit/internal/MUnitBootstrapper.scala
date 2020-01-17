package munit.internal

import org.scalajs.junit.Bootstrapper
import org.scalajs.junit.TestMetadata
import scala.concurrent.Future
import scala.util.Try
import org.junit.Test
import scala.util.Success

class MUnitBootstrapper(suite: munit.Suite) extends Bootstrapper {
  def beforeClass(): Unit = ()
  def afterClass(): Unit = ()
  def before(instance: AnyRef): Unit = ()
  def after(instance: AnyRef): Unit = ()

  def tests(): Array[TestMetadata] =
    Array(new TestMetadata("hello", false, new Test()))

  def invokeTest(instance: AnyRef, name: String): Future[Try[Unit]] =
    Future.successful {
      println("Hello world!")
      Success(())
    }

  def newInstance(): AnyRef = suite
}

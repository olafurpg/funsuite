package munit.internal

import com.geirsson.junit
import com.geirsson.junit.TestMetadata
import org.scalajs.junit.Bootstrapper

import scala.concurrent.Future
import scala.util.Try
import org.junit.Test

import scala.util.Success

class MUnitBootstrapper(suite: munit.Suite) extends junit.Bootstrapper {
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

package com.geirsson.junit

import scala.concurrent.Future
import scala.scalajs.reflect.annotation.EnableReflectiveInstantiation
import scala.util.Try

/** Scala.js internal JUnit bootstrapper.
 *
 *  This class is public due to implementation details. Only the junit compiler
 *  plugin may generate classes inheriting from it.
 *
 *  Relying on this trait directly is unspecified behavior.
 */
@EnableReflectiveInstantiation
trait Bootstrapper {
  def beforeClass(): Unit
  def afterClass(): Unit
  def before(instance: AnyRef): Unit
  def after(instance: AnyRef): Unit

  def tests(): Array[TestMetadata]
  def invokeTest(instance: AnyRef, name: String): Future[Try[Unit]]

  def newInstance(): AnyRef
}

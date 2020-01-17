package munit.internal

import munit.Location
import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

object MacroCompat {

  trait LocationMacro {
    implicit def generateLocation: Location = macro locationImpl
  }

  def locationImpl(c: Context): c.Tree = {
    import c.universe._
    val line = Literal(Constant(c.enclosingPosition.line))
    val path = Literal(Constant(c.enclosingPosition.source.path))
    New(typeOf[Location], path, line)
  }
}

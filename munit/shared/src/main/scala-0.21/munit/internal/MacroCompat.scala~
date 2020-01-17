package munit.internal

import munit.Location
import scala.quoted._

object MacroCompat {

  trait LocationMacro {
    inline implicit def generateLocation: Location = ${ locationImpl() }
  }

  def locationImpl()(given qctx: QuoteContext): Expr[Location] = {
    import qctx.tasty.{_, given}
    val path = rootPosition.sourceFile.jpath.toString
    val startLine = rootPosition.startLine
    '{ new Location(${Expr(path)}, ${Expr(startLine)}) }
  }

}

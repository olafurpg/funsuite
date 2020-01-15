package munit

import java.lang.annotation.Annotation
import scala.runtime.Statics

class Tag(val value: String) extends Annotation with Serializable {
  // Not a case class so that it possible to override these.
  override def equals(obj: Any): Boolean = obj match {
    case t: Tag => t.value == value
    case _      => false
  }
  override def hashCode(): Int = {
    var acc: Int = -881232
    acc = Statics.mix(acc, Statics.anyHash("munit.Tag"))
    acc = Statics.mix(acc, Statics.anyHash(value))
    acc
  }
  def annotationType(): Class[_ <: Annotation] = this.getClass()
  override def toString(): String = s"Tag($value)"
}

package munit

import scala.runtime.Statics

/**
  * Metadata about a single test case.
  *
  * @param body the function to be evaluated for this test case.
  * @param tags the annotated tags for this test case.
  * @param location the file and line number where this test was defined.
  */
class GenericTest[T](
    val name: String,
    val body: () => T,
    val tags: Set[Tag],
    val location: Location
) extends Serializable {
  def this(name: String, body: () => T)(implicit loc: Location) =
    this(name, body, Set.empty, loc)
  def withName(newName: String): GenericTest[T] =
    copy(name = newName)
  def withBody[A](newBody: () => A): GenericTest[A] =
    copy(body = newBody)
  def withTags(newTags: Set[Tag]): GenericTest[T] =
    copy(tags = newTags)
  def withLocation(newLocation: Location): GenericTest[T] =
    copy(location = newLocation)
  private[this] def copy[A](
      name: String = this.name,
      body: () => A = this.body,
      tags: Set[Tag] = this.tags,
      location: Location = this.location
  ): GenericTest[A] = {
    new GenericTest(name, body, tags, location)
  }
  override def toString(): String = s"GenericTest($name)"
  override def equals(obj: Any): Boolean = {
    obj.asInstanceOf[AnyRef].eq(this) || (obj match {
      case t: GenericTest[_] =>
        t.name == name &&
          // skip body
          t.tags == tags &&
          t.location == location
      case _ =>
        false
    })
  }
  override def hashCode(): Int = {
    var acc = 23482342
    acc = Statics.mix(acc, Statics.anyHash(name))
    // skip body
    acc = Statics.mix(acc, Statics.anyHash(tags))
    acc = Statics.mix(acc, Statics.anyHash(location))
    acc
  }
}

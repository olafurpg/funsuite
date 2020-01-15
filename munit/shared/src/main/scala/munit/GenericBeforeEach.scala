package munit

class GenericBeforeEach[T](
    val test: GenericTest[T]
) extends Serializable

class GenericAfterEach[T](
    val test: GenericTest[T]
) extends Serializable

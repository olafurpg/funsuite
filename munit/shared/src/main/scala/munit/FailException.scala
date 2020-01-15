package munit

class FailException(
    message: String,
    cause: Throwable,
    isStackTracesEnabled: Boolean,
    location: Location
) extends Exception(message, cause)
    with Serializable {
  def this(message: String, location: Location) =
    this(message, null, true, location)
  def this(message: String, cause: Throwable, location: Location) = this(
    message,
    cause,
    true,
    location
  )
  override def fillInStackTrace(): Throwable = {
    val result = super.fillInStackTrace()
    result.setStackTrace(result.getStackTrace().slice(0, 1))
    result
  }
}

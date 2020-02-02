package munit.internal

import scala.concurrent.Future
import scala.util.Try
import scala.concurrent.ExecutionContext

object FutureCompat {
  implicit class ExtensionFuture[T](f: Future[T]) {
    def transformTry[B](
        fn: Try[T] => Try[B]
    )(implicit ec: ExecutionContext): Future[B] = {
      f.transform(fn)
    }
  }
}

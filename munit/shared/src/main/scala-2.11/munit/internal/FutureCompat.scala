package munit.internal

import scala.concurrent.Future
import scala.util.Try
import scala.concurrent.ExecutionContext
import scala.concurrent.Promise

object FutureCompat {
  implicit class ExtensionFuture[T](f: Future[T]) {
    def transformTry[B](
        fn: Try[T] => Try[B]
    )(implicit ec: ExecutionContext): Future[B] = {
      val p = Promise[B]()
      f.onComplete { t =>
        p.complete(fn(t))
      }
      p.future
    }
  }
}

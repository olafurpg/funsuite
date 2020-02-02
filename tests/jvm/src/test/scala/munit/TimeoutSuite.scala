package munit

import scala.concurrent.duration.Duration
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.Await

class TimeoutSuite extends munit.FunSuite {
  override val munitTimeout: FiniteDuration = Duration(100, "ms")
  override def munitTestValue(testValue: Any): Future[Any] = {
    Future.successful(
      Await.result(
        super.munitTestValue(testValue),
        munitTimeout
      )
    )
  }
  test("slow".fail) {
    Future {
      Thread.sleep(1000)
    }
  }
  test("fast") {
    Future {
      Thread.sleep(1)
    }
  }
}

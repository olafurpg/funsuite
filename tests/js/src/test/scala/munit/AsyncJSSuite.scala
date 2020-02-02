package munit

import scala.concurrent.Promise

class AsyncJSSuite extends FunSuite {
  test("async-ok") {
    val p = Promise[Unit]()
    scala.scalajs.js.timers.setTimeout(100) {
      p.success(())
    }
    p.future
  }

  test("async-error".fail) {
    val p = Promise[Unit]()
    scala.scalajs.js.timers.setTimeout(100) {
      p.failure(new RuntimeException("boom"))
    }
    p.future
  }
}

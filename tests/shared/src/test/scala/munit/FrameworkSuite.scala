package munit

import java.io.ByteArrayOutputStream
import sbt.testing.TaskDef
import sbt.testing.EventHandler
import sbt.testing.Event
import java.io.PrintStream
import com.geirsson.junit.Ansi
import java.nio.charset.StandardCharsets
import sbt.testing.Logger
import sbt.testing.Status
import scala.util.Properties

class FrameworkSuite extends BaseFrameworkSuite {
  val tests = List[FrameworkTest](
    InterceptFrameworkSuite,
    CiOnlyFrameworkSuite,
    DiffProductFrameworkSuite,
    FailFrameworkSuite,
    DuplicateNameFrameworkSuite
  )
  tests.foreach { t =>
    check(t.cls, t.expected)(t.location)
  }
}

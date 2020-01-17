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
import scala.util.control.NonFatal

abstract class BaseFrameworkSuite extends FunSuite {
  override def munitIgnore = !BuildInfo.scalaVersion.startsWith("2.13")
  def exceptionMessage(ex: Throwable): String = {
    if (ex.getMessage() == null) "null"
    else {
      ex.getMessage()
        .replaceAllLiterally(
          BuildInfo.sourceDirectory.toString(),
          ""
        )
        .replace('\\', '/')
    }
  }
  def check(cls: Class[_], expected: String)(implicit loc: Location): Unit = {
    test(cls.getSimpleName()) {
      val baos = new ByteArrayOutputStream()
      val out = new PrintStream(baos)
      val logger = new Logger {
        def ansiCodesSupported(): Boolean = false
        def error(x: String): Unit = out.println(x)
        def warn(x: String): Unit = out.println(x)
        def info(x: String): Unit = out.println(x)
        def debug(x: String): Unit = out.println(x)
        def trace(x: Throwable): Unit = out.println(x)
      }
      val framework = new Framework
      val runner = framework.runner(
        Array("+l"), // use sbt loggers
        Array(),
        this.getClass().getClassLoader()
      )
      val tasks = runner.tasks(
        Array(
          new TaskDef(
            cls.getCanonicalName(),
            framework.munitFingerprint,
            false,
            Array()
          )
        )
      )
      val events = new StringBuilder()
      val eventHandler = new EventHandler {
        def handle(event: Event): Unit = {
          try {
            val status = event.status().toString().toLowerCase()
            val name = event.fullyQualifiedName()
            events
              .append("==> ")
              .append(status)
              .append(" ")
              .append(name)
            if (event.throwable().isDefined()) {
              events
                .append(" - ")
                .append(exceptionMessage(event.throwable().get()))
            }
            events.append("\n")
          } catch {
            case NonFatal(e) =>
              e.printStackTrace()
              events.append(s"unexpected error: $e")
          }
        }
      }
      tasks.foreach(_.execute(eventHandler, Array(logger)))
      val stdout = Ansi
        .filterAnsi(baos.toString(StandardCharsets.UTF_8.name()))
      val obtained = Ansi.filterAnsi(
        events.toString().replaceAllLiterally("\"\"\"", "'''")
      )
      assertNoDiff(
        obtained,
        expected,
        stdout
      )
    }
  }
}

package docs

import munit.docs.StorageProxy
import mdoc.PreModifier
import mdoc.PreModifierContext
import scala.util.control.NonFatal
import java.{util => ju}
import com.google.cloud.storage.StorageException
import com.google.cloud.storage.StorageOptions
import scala.collection.mutable
import scala.collection.JavaConverters._
import com.google.cloud.storage.Blob
import java.io.ByteArrayOutputStream
import com.google.gson.Gson
import java.nio.charset.StandardCharsets
import munit.sbtmunit.MUnitTestReport
import sbt.testing.Status
import munit.TestValues.FlakyFailure

class MUnitModifier extends PreModifier {
  val name: String = "munit"
  def process(ctx: PreModifierContext): String = {
    html.getOrElse {
      ctx.reporter.warning("Unable to generate MUnit HTML test report.")
      ""
    }
  }

  private val properties: Map[String, String] =
    try {
      val props = new ju.Properties()
      props.load(
        this
          .getClass()
          .getClassLoader()
          .getResourceAsStream("munit.properties")
      )
      val m = mutable.Map.empty[String, String]
      props.forEach((k, v) => m(k.toString()) = v.toString())
      m.toMap
    } catch {
      case NonFatal(e) =>
        e.printStackTrace()
        Map.empty
    }
  lazy val html: Option[String] =
    try {
      for {
        bucketName <- properties.get("munitBucketName")
        repository <- properties.get("munitRepository")
      } yield {
        val storage = StorageOptions.getDefaultInstance().getService()
        val blobs = StorageProxy
          .list(storage, bucketName, repository)
          .iterateAll()
          .asScala
        renderHtml(blobs)
      }
    } catch {
      case NonFatal(e) =>
        e match {
          case s: StorageException
              if e.getMessage() != "Anonymous caller does not have storage.objects.list access to munit-test-reports" =>
          case _ =>
            e.printStackTrace()
        }
        None
    }

  private def downloadBlob(blob: Blob): Option[MUnitTestReport.Summary] = {
    try {
      val out = new ByteArrayOutputStream()
      blob.downloadTo(out)
      val summary = new Gson().fromJson(
        new String(out.toByteArray(), StandardCharsets.UTF_8),
        classOf[MUnitTestReport.Summary]
      )
      Some(summary)
    } catch {
      case NonFatal(e) =>
        e.printStackTrace()
        None
    }
  }

  private def renderHtml(blobs: Iterable[Blob]): String = {
    val tests =
      mutable.Map.empty[String, mutable.ArrayBuffer[MUnitTestReport.TestEvent]]
    for {
      blob <- blobs.iterator
      summary <- downloadBlob(blob).iterator
      group <- summary.groups.iterator
      event <- group.events.toIterable
    } {
      val buffer =
        tests.getOrElseUpdate(event.name, mutable.ArrayBuffer.empty)
      buffer += event
    }
    val statuses = Array(
      Status.Success,
      Status.Failure,
      Status.Skipped,
      Status.Ignored
    )
    val statusHeaders = statuses.map(s => <th>{s}</th>)
    val keys = tests.keys.toIndexedSeq.sorted
    val testRows = keys.map { name =>
      val events = tests(name)
      val statusMap = events.groupBy(_.status).mapValues(_.size)
      val passedCount = statusMap.getOrElse(Status.Success.toString(), 0)
      val errorCount = statusMap.getOrElse(Status.Failure.toString(), 0)
      val flakyCount = events.count { event =>
        event.exception != null &&
        Status.Skipped.toString == event.status &&
        classOf[FlakyFailure].getName() == event.exception.className
      }
      val skippedCount =
        statusMap.getOrElse(Status.Skipped.toString(), 0) - flakyCount
      val ignoredCount = statusMap.getOrElse(Status.Ignored.toString(), 0)
      val statusColumns = statuses.map { s =>
        statusMap.getOrElse(s.toString(), 0)
      }
      val otherCount = events.length -
        passedCount -
        errorCount -
        flakyCount -
        skippedCount -
        ignoredCount
      val flakyRatio: Double =
        if (events.length == 0) 0
        else {
          val r = (errorCount + flakyCount).toDouble / events.length
          if (r == 0) 0
          else r
        }
      val totalCount = events.length
      val averageDuration =
        if (totalCount == 0) 0
        else events.iterator.map(_.duration).filter(_ >= 0).sum / totalCount
      val shortName =
        if (name.length() > 80) name.take(80) + "..."
        else name
      <tr>
        <td title={name}>{shortName}</td>
        <td>{averageDuration}</td>
        <td>{passedCount}</td>
        <td>{errorCount}</td>
        <td>{flakyCount}</td>
        <td>{flakyRatio}</td>
        <td>{skippedCount}</td>
        <td>{ignoredCount}</td>
        <td>{otherCount}</td>
      </tr>
    }
    val table =
      <table id="munit" class="display">
      <col width="300"/>
      <thead>
      <tr>
      <th>Name</th>
      <th>D</th>
      <th>P</th>
      <th>E</th>
      <th>F</th>
      <th>R</th>
      <th>S</th>
      <th>I</th>
      <th>O</th>
      </tr>
      </thead>
      <tbody>
        {testRows}
      </tbody>
    </table>
    s"""
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
<link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/1.10.20/css/jquery.dataTables.css">
<script type="text/javascript" charset="utf8" src="https://cdn.datatables.net/1.10.20/js/jquery.dataTables.js"></script>

**D**: Average duration in milliseconds, 
**P**: Passed, 
**E**: Failed, 
**F**: Flaky,
**R**: Flaky/Passed ratio,
**S**: Skipped, 
**I**: Ignored, 
**O**: Other

$table

<script>
  $$(document).ready( function () {
    $$('#munit').DataTable({
      paging: false,
    });
  });
</script>
"""
  }
}

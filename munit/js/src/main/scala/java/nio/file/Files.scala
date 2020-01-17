package java.nio.file

import java.{util => ju}
import scala.meta.internal.io.JSIO
import scala.meta.internal.io.JSFs
import java.nio.charset.StandardCharsets
import scala.collection.JavaConverters._

object Files {
  def readAllLines(path: Path): ju.List[String] = {
    val bytes = readAllBytes(path)
    val text = new String(bytes, StandardCharsets.UTF_8)
    text.linesIterator.toSeq.asJava
  }
  def readAllBytes(path: Path): Array[Byte] = JSIO.inNode {
    val jsArray = JSFs.readFileSync(path.toString)
    val len = jsArray.length
    val result = new Array[Byte](len)
    var curr = 0
    while (curr < len) {
      result(curr) = jsArray(curr).toByte
      curr += 1
    }
    result
  }
}

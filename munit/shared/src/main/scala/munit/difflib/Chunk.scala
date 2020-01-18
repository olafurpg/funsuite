package munit.difflib

import java.util

class Chunk[T](position: Int, lines: util.List[T]) {

  def getPosition: Int = position
  def getLines: util.List[T] = lines
  def size: Int = lines.size()

  override def toString = s"Chunk($getPosition, $getLines, $size)"
}

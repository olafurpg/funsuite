package munit

import java.io.PrintStream

object TestingConsole {
  var out: PrintStream = System.out
  var err: PrintStream = System.err
}

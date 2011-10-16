package org.proofcafe.android.socketserver

object Interpreter {
  def lineInterpreter(lines : Iterator[String]): Iterator[String] = {
    lines takeWhile {
      line => line != "exit"
    } filter {
      line => line != ""
    } map {
      line => "FOO: "+line
    }
  }
}

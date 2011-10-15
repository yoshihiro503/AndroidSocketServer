package org.proofcafe.android.socketserver

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import android.widget.LinearLayout
import android.util.Log
import java.io.PrintWriter
import scala.io.Source
import scala.actors.Actor

class MainActivity extends Activity {
  val TAG = "MainActivity"
  val port = 8080

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(new TextView(this) {
      setText("address = ["+getAddress.mkString(",")+"], port = "+port)
    })

    forever(server)
  }

  def forever(f : () => Unit) = {
    import scala.actors.Actor._
    actor (loop(f())) start
  }

  def getLines[T](in : java.io.InputStream) : Stream[String] = {
    import java.io._
    val reader = new BufferedReader(new InputStreamReader(in))
    Stream.continually(reader.readLine()).takeWhile(_ != null)
  }

  def server() = {
    import java.net.ServerSocket

    val server = new ServerSocket(port) {
      setSoTimeout(0)
    }
    val socket = server.accept()
    val in  = socket.getInputStream()
    val out = new PrintWriter(socket.getOutputStream())
    try {
      Interpreter.lineInterpreter(getLines(in)).foreach {
      	res =>
	  out.println(res)
	  out.flush()
      }
    } catch {
      case e => e.printStackTrace()
    }
    server.close
    in.close()
    out.close()
  }

  def getAddress() : Iterator[java.net.InetAddress] = {
    import java.net.NetworkInterface
    import scala.collection.JavaConversions._

    for {
      network <- NetworkInterface.getNetworkInterfaces();
      address <- network.getInetAddresses()
      if("127.0.0.1" != address.getHostAddress())
      if("0.0.0.0" != address.getHostAddress())
    } yield (address)
  }
}

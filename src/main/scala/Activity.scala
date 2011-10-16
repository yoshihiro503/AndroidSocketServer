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

  def getLines(in : java.io.InputStream) : Iterator[String] = {
    import java.io._
    val reader = new BufferedReader(new InputStreamReader(in))
    Iterator.continually(reader.readLine()).takeWhile(_ != null)
  }

  def server() = {
    import java.net.ServerSocket
    import java.net.Socket
    import java.io._
    def using[T](op : (InputStream,OutputStream) => T) : T = {
      val server = new ServerSocket(port) {
        setSoTimeout(0)
      }
      val socket = server.accept()
      val in =  socket.getInputStream()
      val out = socket.getOutputStream()
      def close() = { server.close(); in.close(); out.close() }
      try {
        val y = op(in, out)
        close(); y
      } catch {
        case (e: Exception) => { close(); throw e }
      }
    }
    using {
      case(in, out) =>
        val writer = new PrintWriter(out)
        Interpreter.lineInterpreter(getLines(in)).foreach {
      	  res =>
	    writer.println(res)
	    writer.flush()
        }
    }
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

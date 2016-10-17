package uk.gov.hmrc.clamav.fake

import java.io.{BufferedReader, DataOutputStream, IOException, InputStreamReader}
import java.net.ServerSocket

import play.api.Logger
import uk.gov.hmrc.clamav.ClamAntiVirus

import scala.concurrent.{ExecutionContext, Future}

class FakeClam(serverSocket: ServerSocket)(implicit executionContext: ExecutionContext) {

  def start(): Future[Unit] = {
    Future {
      Logger.debug(s"Fake Clam started on port ${ serverSocket.getLocalPort }")
      while (! serverSocket.isClosed) {
        try {
          val socket = serverSocket.accept()
          val outputStream = socket.getOutputStream
          val dataOutputStream = new DataOutputStream(outputStream)
          val in = new BufferedReader(new InputStreamReader(socket.getInputStream))

          handle(in, dataOutputStream)

          dataOutputStream.close()
          outputStream.close()
          socket.close()
        } catch {
          case _ :IOException => Logger.debug("IOException reading from the socket")
        }
      }
    }
  }

  private def handle(in: BufferedReader, out: DataOutputStream): Unit = {
    val received = new String(Iterator.continually(in.read)
      .takeWhile(_ != 0)
      .map(_.toByte).toArray)

    received match {
      case "zINSTREAM" | "" => handle(in, out)
      case _ =>
        Logger.debug(s"Responding with ${ClamAntiVirus.okClamAvResponse}")
        out.writeBytes(ClamAntiVirus.okClamAvResponse)
        out.flush()
    }
  }

  def stop() = {
    Logger.debug("Stopping Fake Clam")
    if (! serverSocket.isClosed) serverSocket.close()
  }
}

object FakeClam {

  def apply(port: Int): ServerSocket = {
    new FakeClam(new ServerSocket(port))
  }
}
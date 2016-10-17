/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

  def apply(port: Int): FakeClam = {
    new FakeClam(new ServerSocket(port))
  }
}

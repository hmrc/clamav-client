/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.clamav

import java.io.{DataOutputStream, InputStream}
import java.net.{InetSocketAddress, Socket}

import play.api.Logger
import uk.gov.hmrc.clamav.config.ClamAvConfig

import scala.concurrent.{ExecutionContext, Future}

class ClamAvSocket(socket: Socket, val in : InputStream, val out : DataOutputStream) extends Connection {

  private def terminate()(implicit ec: ExecutionContext): Future[Unit] =
    Future {
      socket.close()
      out.close()
    } recover {
      case e: Throwable =>
        Logger.error("Error closing socket to clamd", e)
    }

}

trait Connection {
  def in: InputStream
  def out: DataOutputStream
}

object ClamAvSocket {

  private def openSocket(config : ClamAvConfig)(implicit ec : ExecutionContext) = Future {
    val sock = new Socket
    sock.setSoTimeout(config.timeout)

    val address: InetSocketAddress = new InetSocketAddress(config.host, config.port)
    sock.connect(address)

    val out: DataOutputStream =
      new DataOutputStream(sock.getOutputStream)

    val in: InputStream = sock.getInputStream

    new ClamAvSocket(sock, in, out)
  }

  def withSocket[T](config: ClamAvConfig)(function: Connection => Future[T])(
    implicit ec: ExecutionContext): Future[T] = {
    for {
      socket <- openSocket(config)
      result <- {
        val functionResult = function(socket)
        functionResult.onComplete(_ => socket.terminate())
        functionResult
      }
    } yield result
  }
}

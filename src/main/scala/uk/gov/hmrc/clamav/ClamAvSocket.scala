/*
 * Copyright 2018 HM Revenue & Customs
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

class ClamAvSocket(config: ClamAvConfig) {

  private lazy val socket: Socket = openSocket()

  private lazy val toClam: DataOutputStream =
    new DataOutputStream(socket.getOutputStream)

  private lazy val fromClam: InputStream = socket.getInputStream

  private def openSocket(): Socket = {
    val sock = new Socket
    sock.setSoTimeout(config.timeout)

    val address: InetSocketAddress = new InetSocketAddress(config.host, config.port)
    sock.connect(address)
    sock
  }

  private def terminate()(implicit ec: ExecutionContext): Future[Unit] =
    Future {
      socket.close()
      toClam.close()
    } recover {
      case e: Throwable =>
        Logger.error("Error closing socket to clamd", e)
    }

}

case class Connection(in: InputStream, out: DataOutputStream)

object ClamAvSocket {
  def withSocket[T](config: ClamAvConfig)(function: Connection => Future[T])(
    implicit ec: ExecutionContext): Future[T] = {

    val socket = new ClamAvSocket(config)
    val result = function.apply(Connection(socket.fromClam, socket.toClam))
    result.onComplete(_ => socket.terminate())
    result
  }
}

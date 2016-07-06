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

package uk.gov.hmrc.clamav

import java.io._

import play.api.Logger
import uk.gov.hmrc.clamav.config.ClamAvConfig
import uk.gov.hmrc.clamav.config.ClamAvConfig._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

trait ClamAvResponseInterpreter {
  def interpretResponseFromClamd(responseFromClamd: Option[String])(implicit clamAvConfig: ClamAvConfig): Try[Boolean] = {
    responseFromClamd match {
      case Some(`okClamAvResponse`) =>
        Logger.info("File clean")
        Success(true)
      case Some(responseString) =>
        Logger.warn(s"Virus detected : $responseString")
        Failure(new VirusDetectedException(responseString))
      case None =>
        Logger.warn("Empty response from clamd")
        Failure(new VirusScannerFailureException("Empty response from clamd"))
    }
  }
}

trait ClamAvSocket {
  val config: ClamAvConfig

  lazy val socket = openSocket()

  lazy val toClam = {
    val ds = new DataOutputStream(socket.getOutputStream)
    ds.write(instream.getBytes())
    ds
  }

  lazy val fromClam = socket.getInputStream

  def openSocket() = {
    import java.net.{InetSocketAddress, Socket}
    val sock = new Socket
    sock.setSoTimeout(config.timeout)
    val address: InetSocketAddress = new InetSocketAddress(config.host, config.port)
    Logger.debug(s"Attempting connection to : $address")
    sock.connect(address)
    sock
  }
}

class ClamAntiVirus()(implicit clamAvConfig: ClamAvConfig) extends ClamAvResponseInterpreter with VirusChecker with ClamAvSocket {
  override val config: ClamAvConfig = clamAvConfig

  override def send(bytes: Array[Byte])(implicit ec: ExecutionContext): Future[Unit] = {
    Future {
      toClam.writeInt(bytes.length)
      toClam.write(bytes)
      toClam.flush()
    }
  }

  override def finish()(implicit ec: ExecutionContext): Future[Try[Boolean]] = {
    for {
      _ <- Future { toClam.writeInt(0); toClam.flush() }
      r <- Future(interpretResponseFromClamd(responseFromClamd()))
      _ <- Future(terminate())
    } yield r
  }

  private[clamav] def terminate() = {
    val r = for {
      _ <- Try(socket.close())
      r <- Try(toClam.close())
    } yield r

    r match {
      case Success(_) => ()
      case Failure(e) => Logger.warn("Error closing socket to clamd", e)
    }
  }

  private def responseFromClamd(): Option[String] = {
    val response = new String(
      Iterator.continually(fromClam.read)
        .takeWhile(_ != -1)
        .map(_.toByte)
        .toArray)

    Logger.info(s"Response from clamd: $response")
    emptyToNone(response)
  }

  def emptyToNone(s: String): Option[String] = if (s.trim.isEmpty) None else Some(s)
}


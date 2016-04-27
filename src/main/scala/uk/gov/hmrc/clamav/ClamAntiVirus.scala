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
import uk.gov.hmrc.clamav.config.{ClamAvSocket, ClamAvConfig}

import scala.concurrent.{ExecutionContext, Future}

// This is a fork of https://github.com/davidillsley/gds-clamav-scala/tree/531562368a438eafc1fcbfa07cc63c184d369fa9
trait ClamAvResponseInterpreter {

  def interpretResponseFromClamd(responseFromClamd: Option[String])(implicit clamAvConfig: ClamAvConfig): Unit = {
    responseFromClamd match {
      case Some(clamAvConfig.okClamAvResponse) =>
        Logger.info("File clean")
      case None =>
        Logger.warn("Empty response from clamd")
        throw new VirusScannerFailureException("Empty response from clamd")
      case Some(responseString) =>
        Logger.warn(s"Virus detected : $responseString")
        throw new VirusDetectedException(responseString)
    }
  }
}


class ClamAntiVirus()(implicit clamAvConfig: ClamAvConfig) extends ClamAvResponseInterpreter with VirusChecker with ClamAvSocket {

  private val socket = openSocket
  private val toClam = new DataOutputStream(socket.getOutputStream)
  private lazy val fromClam = socket.getInputStream

  toClam.write(clamAvConfig.instream.getBytes())

  override def send(bytes: Array[Byte])(implicit ec : ExecutionContext): Future[Unit] = {
    Future{
      toClam.writeInt(bytes.length)
      toClam.write(bytes)
      toClam.flush()
    }
  }

  override def finish()(implicit ec : ExecutionContext): Future[Unit] = {
    Future {
      try {
        toClam.writeInt(0)
        toClam.flush()

        interpretResponseFromClamd(responseFromClamd())
      }
      finally {
        terminate()
      }
    }
  }

  private [clamav] def terminate() = {
    try {
      socket.close()
    } catch {
      case e: IOException =>
        Logger.warn("Error closing socket to clamd", e)
    }

    try {
      toClam.close()
    } catch {
      case e: IOException =>
        Logger.warn("Error closing socket to clamd", e)
    }
  }

  private def responseFromClamd(): Option[String] = {
    val response = new String(
      Iterator.continually(fromClam.read)
        .takeWhile(_ != -1)
        .map(_.toByte)
        .toArray)

    Logger.info(s"Response from clamd: $response")
    emptyToNone(response.trim)
  }

  def emptyToNone(s: String): Option[String] = {
    if (s.isEmpty) None else Some(s)
  }
}


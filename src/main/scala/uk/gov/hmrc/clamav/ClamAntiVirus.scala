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

import play.api.Logger
import uk.gov.hmrc.clamav.config.ClamAvConfig
import uk.gov.hmrc.clamav.model.{VirusDetectedException, VirusScannerFailureException}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}


class ClamAntiVirus(clamAvConfig: ClamAvConfig) {

  private val clamAvSocket: ClamAvSocket = new ClamAvSocket(clamAvConfig)
  private val okClamAvResponse = "stream: OK\u0000"

  def sendAndCheck(bytes: Array[Byte])(implicit ec: ExecutionContext): Future[Try[Unit]] = {
    for {
      _ <- sendBytes(bytes)
      checked <- checkForVirus()
    } yield checked
  }

  private def sendBytes(bytes: Array[Byte])(implicit ec: ExecutionContext) = {
    Future {
      clamAvSocket.toClam.writeInt(bytes.length)
      clamAvSocket.toClam.write(bytes)
      clamAvSocket.toClam.flush()
    }
  }

  private def checkForVirus()(implicit ec: ExecutionContext): Future[Try[Unit]] = {
    for {
      result <- Future {
        clamAvSocket.toClam.writeInt(0)
        clamAvSocket.toClam.flush()
        readResponseFromClamd() match {
          case Some(response) => response match {
            case `okClamAvResponse` => Success(())
            case badResponse => Failure(new VirusDetectedException(badResponse))
          }
          case None => Failure(new VirusScannerFailureException("Empty response from clamd"))
        }
      }
      _ <- Future(terminate())
    } yield result
  }

  private def terminate(): Try[Unit] = {
    Try {
      clamAvSocket.socket.close()
      clamAvSocket.toClam.close()
    } recover { case e: Throwable =>
      Logger.error("Error closing socket to clamd", e)
    }
  }

  private def readResponseFromClamd(): Option[String] = {
    val response = Option(new String(
      Iterator.continually(clamAvSocket.fromClam.read)
        .takeWhile(_ != -1)
        .map(_.toByte)
        .toArray))

    response flatMap {
      case s if s.trim.isEmpty || s == null => None
      case s => Some(s)
    }
  }
}

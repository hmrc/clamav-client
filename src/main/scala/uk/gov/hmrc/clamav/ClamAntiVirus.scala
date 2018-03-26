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

import org.apache.commons.io.IOUtils
import play.api.Logger
import uk.gov.hmrc.clamav.config.ClamAvConfig
import uk.gov.hmrc.clamav.model._

import scala.concurrent.{ExecutionContext, Future}

class ClamAntiVirus private[clamav] (clamAvConfig: ClamAvConfig)(implicit ec: ExecutionContext) {

  private val clamAvSocket: ClamAvSocket = new ClamAvSocket(clamAvConfig)
  private val FileCleanResponse          = "stream: OK\u0000"
  private val VirusFoundResponse         = "stream\\: (.+) FOUND\u0000".r
  private val ParseableErrorResponse     = "(.+) ERROR\u0000".r

  def sendAndCheck(bytes: Array[Byte])(implicit ec: ExecutionContext): Future[ScanningResult] =
    for {
      _              <- sendRequest(bytes)
      response       <- readResponse()
      parsedResponse <- parseResponse(response)
      _              <- terminate()
    } yield parsedResponse

  private def sendRequest(bytes: Array[Byte])(implicit ec: ExecutionContext) = Future {
    clamAvSocket.toClam.writeInt(bytes.length)
    clamAvSocket.toClam.write(bytes)
    clamAvSocket.toClam.writeInt(0)
    clamAvSocket.toClam.flush()
  }

  private def readResponse(): Future[String] = Future {
    IOUtils.toString(clamAvSocket.fromClam)
  }

  private def parseResponse(response: String) =
    response match {
      case FileCleanResponse             => Future.successful(Clean)
      case VirusFoundResponse(virus)     => Future.successful(Infected(virus))
      case ParseableErrorResponse(error) => Future.failed(new ClamAvException(error))
      case unparseableResponse =>
        Future.failed(new ClamAvException(s"Unparseable response from ClamAV: $unparseableResponse"))
    }

  private def terminate(): Future[Unit] =
    Future {
      clamAvSocket.socket.close()
      clamAvSocket.toClam.close()
    } recover {
      case e: Throwable =>
        Logger.error("Error closing socket to clamd", e)
    }

}

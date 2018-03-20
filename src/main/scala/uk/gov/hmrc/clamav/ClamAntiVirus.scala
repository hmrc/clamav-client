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

import javax.inject.Inject

import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try


class ClamAntiVirus @Inject()(responseInterpreter: ClamAvResponseInterpreter,
                              clamAvSocket: ClamAvSocket) {

  def send(bytes: Array[Byte])(implicit ec: ExecutionContext): Future[Unit] = {
    Future {
      clamAvSocket.toClam.writeInt(bytes.length)
      clamAvSocket.toClam.write(bytes)
      clamAvSocket.toClam.flush()
    }
  }

  def checkForVirus()(implicit ec : ExecutionContext): Future[Try[Boolean]] = {
    for {
      result <- Future {
        clamAvSocket.toClam.writeInt(0)
        clamAvSocket.toClam.flush()
        responseFromClamd map responseInterpreter.interpretResponseFromClamd getOrElse responseInterpreter.invalidResponse
      }
      _ <- Future(terminate())
    } yield result
  }

  private def noneIfEmpty: PartialFunction[String, Option[String]] = {
    case s if s.trim.isEmpty || s == null => None
    case s => Some(s)
  }

  private[clamav] def terminate() = {
    Try {
      clamAvSocket.socket.close()
      clamAvSocket.toClam.close()
    } recover logWarning("Error closing socket to clamd")
  }

  private def responseFromClamd(): Option[String] = {
    val response = Option(new String(
      Iterator.continually(clamAvSocket.fromClam.read)
        .takeWhile(_ != -1)
        .map(_.toByte)
        .toArray))

    Logger.info(s"Response from clamd: $response")
    response flatMap noneIfEmpty
  }

  private def logWarning(msg: String): PartialFunction[Throwable, Unit] = {
    case e => Logger.warn(msg, e)
  }


}

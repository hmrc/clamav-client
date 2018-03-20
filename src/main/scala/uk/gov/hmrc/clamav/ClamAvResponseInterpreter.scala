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
import uk.gov.hmrc.clamav.model.{VirusDetectedException, VirusScannerFailureException}

import scala.util.{Failure, Success, Try}

class ClamAvResponseInterpreter {
  private val okClamAvResponse = "stream: OK\u0000"

  def interpretResponseFromClamd: PartialFunction[String, Try[Boolean]] = {
    case `okClamAvResponse` =>
      Logger.info("File clean")
      Success(true)
    case responseString =>
      Logger.warn(s"Virus detected : $responseString")
      Failure(new VirusDetectedException(responseString))
  }

  def invalidResponse = {
    Logger.warn("Empty response from clamd")
    Failure(new VirusScannerFailureException("Empty response from clamd"))
  }
}

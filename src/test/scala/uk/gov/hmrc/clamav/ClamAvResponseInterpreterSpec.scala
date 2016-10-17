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

import uk.gov.hmrc.clamav.config.ClamAvConfig
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.util.{Failure, Success}

class ClamAvResponseInterpreterSpec extends UnitSpec with WithFakeApplication {
  val interpreter = new ClamAvResponseInterpreter {}

  val clamAvConfig = ClamAvConfig(33769, "avscan", 3310, 5000, 29, 10485760)

  "Interpreting responses from ClamAV" should {
    "return Success(true) on an OK response" in {
      interpreter.interpretResponseFromClamd("stream: OK\u0000") shouldBe Success(true)
    }

    "throw a Failure(_:VirusDetectedException) on a FOUND response" in {
      interpreter.interpretResponseFromClamd("stream: Eicar-Test-Signature FOUND") shouldBe Failure(_:VirusDetectedException)
    }

    "return a Failure(_:ClamAvFailedException) on an empty response" in {
      interpreter.invaldResponse shouldBe Failure(_:VirusScannerFailureException)
    }
  }
}

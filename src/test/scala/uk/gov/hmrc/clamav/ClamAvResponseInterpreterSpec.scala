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

class ClamAvResponseInterpreterSpec extends UnitSpec with WithFakeApplication {
  val interpreter = new ClamAvResponseInterpreter {}

  val clamAvConfig = new ClamAvConfig(true, 33769, "avscan", 3310, 5000, 29)

  "Interpreting responses from ClamAV" should {
    "return without exception on an OK response" in {
      interpreter.interpretResponseFromClamd(Some("stream: OK"))(clamAvConfig)
    }

    "throw a VirusDetectedException on a FOUND response" in {
      intercept[VirusDetectedException] {
        interpreter.interpretResponseFromClamd(Some("stream: Eicar-Test-Signature FOUND"))(clamAvConfig)
      }
    }

    "throw a ClamAvFailedException on an empty response" in {
      intercept[VirusScannerFailureException] {
        // we have observed that when clamav fails under high load we get an
        // empty response
        interpreter.interpretResponseFromClamd(None)(clamAvConfig)
      }
    }
  }
}

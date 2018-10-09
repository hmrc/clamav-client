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

package uk.gov.hmrc.clamav.it

import java.io.ByteArrayInputStream

import uk.gov.hmrc.clamav.ClamAntiVirus
import uk.gov.hmrc.clamav.config.ClamAvConfig
import uk.gov.hmrc.clamav.model.{Clean, Infected}
import uk.gov.hmrc.play.test.UnitSpec

import scala.Array.emptyByteArray

class ClamAvsISpec extends UnitSpec {

  import scala.concurrent.ExecutionContext.Implicits.global

  private val virusSig         = "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*\u0000"
  private val virusFileWithSig = "/eicar-standard-av-test-file"
  private val cleanFile        = "/162000101.pdf"

  private def instance(): ClamAntiVirus = {
    val configuration = new ClamAvConfig {
      override val timeout: Int = 5000
      override val port: Int    = 3310
      override val host: String = "avscan"
    }
    new ClamAntiVirus(configuration)
  }

  "Scanning files" should {
    "allow clean files" in {
      val clamAv = instance()
      val bytes  = FileBytes(cleanFile)

      await(clamAv.sendAndCheck(bytes)) shouldBe Clean
    }

    "allow to scan empty file" in {
      val clamAv = instance()

      await(clamAv.sendAndCheck(emptyByteArray)) shouldBe Clean
    }

    "detect a virus in a file" in {
      val clamAv = instance()
      val bytes  = FileBytes(virusFileWithSig)
      await(clamAv.sendAndCheck(bytes)) shouldBe Infected("Eicar-Test-Signature")
    }

    "allow clean files sent as a stream" in {
      val clamAv = instance()
      val bytes  = FileBytes(cleanFile)

      await(clamAv.sendAndCheck(new ByteArrayInputStream(bytes), bytes.length)) shouldBe Clean
    }

    "detect a virus in a file sent as a stream" in {
      val clamAv = instance()
      val bytes  = FileBytes(virusFileWithSig)

      await(clamAv.sendAndCheck(new ByteArrayInputStream(bytes), bytes.length)) shouldBe Infected(
        "Eicar-Test-Signature")
    }
  }

  "Can scan stream without virus" in {
    val clamAv = instance()

    await(clamAv.sendAndCheck(getBytes(payloadSize = 10000))) shouldBe Clean
  }
  "Can detect a small stream with a virus at the beginning" in {
    val clamAv = instance()

    await(clamAv.sendAndCheck(getBytes(shouldInsertVirusAtPosition = Some(0)))) shouldBe Infected(
      "Eicar-Test-Signature")

  }

  private def getPayload(payloadSize: Int = 0, shouldInsertVirusAtPosition: Option[Int] = None) = {
    val payloadData = shouldInsertVirusAtPosition match {
      case Some(position) =>
        val virusStartPosition = math.min(position, payloadSize - virusSig.length)
        val virusEndPosition   = virusStartPosition + virusSig.length

        0.until(virusStartPosition).map(_ => "a") ++ virusSig ++ virusEndPosition.until(payloadSize).map(_ => "a")

      case _ =>
        0.until(payloadSize).map(_ => "a")
    }

    val payload = payloadData.mkString

    shouldInsertVirusAtPosition match {
      case Some(_) =>
        payload.contains(virusSig) should be(true)
        payload.length             should be(math.max(virusSig.length, payloadSize))
      case _ =>
        payload.length should be(payloadSize)
    }

    payload
  }

  private def getBytes(payloadSize: Int = 0, shouldInsertVirusAtPosition: Option[Int] = None) =
    getPayload(payloadSize, shouldInsertVirusAtPosition).getBytes()

}

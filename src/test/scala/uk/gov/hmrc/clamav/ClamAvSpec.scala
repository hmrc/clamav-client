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

class ClamAvSpec extends UnitSpec with WithFakeApplication {

  import scala.concurrent.ExecutionContext.Implicits.global

  private val virusSig = "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*\u0000"
  private val virusFileWithSig = "/eicar-standard-av-test-file"
  private val cleanFile = "/162000101.pdf"

  def instance(): ClamAntiVirus = {
    val clamAvConfig = ClamAvConfig(true, 33769, "avscan", 3310, 5000, 29, 10485760)
    new ClamAntiVirus()(clamAvConfig)
  }

  "Scanning files" should {
    "allow clean files" in {
      val clamAv = instance()
      val bytes = FileBytes(cleanFile)

      try {
        await(clamAv.send(bytes))
        await(clamAv.checkForVirus())
      }
      finally {
        clamAv.terminate()
      }
    }

    "detect a virus in a file" in {
      val clamAv = instance()
      val bytes = FileBytes(virusFileWithSig)

      try {
        intercept[VirusDetectedException] {
          await(clamAv.send(bytes))
          await(clamAv.checkForVirus())
        }
      }
      finally {
        clamAv.terminate()
      }
    }
  }


  "Can scan stream without virus" in {

    val clamAv = instance()

    try {
      await(clamAv.send(getBytes(payloadSize = 10000)))
      await(clamAv.checkForVirus())
    }
    finally {
      clamAv.terminate()
    }
  }

  "Can stream multiple clean blocks to clam" in {
    val clamAv = instance()

    try {
      await(clamAv.send(getBytes(payloadSize = 1000)))
      await(clamAv.send(getBytes(payloadSize = 1000)))
      await(clamAv.checkForVirus())
    }
    finally {
      clamAv.terminate()
    }
  }

  "Can detect a small stream with a virus at the beginning" in {
    val clamAv = instance()

    try {
      intercept[VirusDetectedException] {
        await(clamAv.send(getBytes(shouldInsertVirusAtPosition = Some(0))))
        await(clamAv.checkForVirus())
      }
    }
    finally {
      clamAv.terminate()
    }
  }

  "emptyToNone" should {
    "convert an empty response to None" in {
      val clamAv = instance()
      clamAv.emptyToNone("") shouldBe None
    }

    "convert a non-empty response to Some(response)" in {
      val clamAv = instance()
      clamAv.emptyToNone("Something") shouldBe Some("Something")
    }
  }

  private def getPayload(payloadSize: Int = 0, shouldInsertVirusAtPosition: Option[Int] = None) = {
    val payloadData = shouldInsertVirusAtPosition match {
      case Some(position) =>
        val virusStartPosition = math.min(position, payloadSize - virusSig.length)
        val virusEndPosition = virusStartPosition + virusSig.length

        0.until(virusStartPosition).map(_ => "a") ++ virusSig ++ virusEndPosition.until(payloadSize).map(_ => "a")

      case _ =>
        0.until(payloadSize).map(_ => "a")
    }

    val payload = payloadData.mkString

    shouldInsertVirusAtPosition match {
      case Some(position) =>
        payload.contains(virusSig) should be(true)
        payload.length should be(math.max(virusSig.length, payloadSize))
      case _ =>
        payload.length should be(payloadSize)
    }

    payload
  }

  private def getBytes(payloadSize: Int = 0,
                       shouldInsertVirusAtPosition: Option[Int] = None) =
    getPayload(payloadSize, shouldInsertVirusAtPosition).getBytes()

}

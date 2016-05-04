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

import play.api.Configuration
import uk.gov.hmrc.clamav.config.LoadClamAvConfig
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class LoadClamAvConfigSpec extends UnitSpec with WithFakeApplication {

  def configuration(enabled: Boolean): Option[Configuration] = {
    Option(Configuration.from(
      Map("enabled" -> enabled , "chunkSize" -> 32768, "host" -> "avscan", "port" -> 3310, "timeout" -> 5000, "threadPoolSize" -> 20))
    )
  }

  def badConfiguration(): Option[Configuration] = {
    Option(Configuration.from(
      Map("chunkSize" -> 32768, "host" -> "avscan", "port" -> 3310, "timeout" -> 5000, "threadPoolSize" -> 20))
    )
  }

  "Test the LoadClamAvConfig" should {
    "load the ClamConfig from the play application config if present" in {
      val clamAvConfig = LoadClamAvConfig(configuration = configuration(true))
      clamAvConfig.chunkSize shouldBe 32768
      clamAvConfig.enabled shouldBe true
      clamAvConfig.host shouldBe "avscan"
      clamAvConfig.port shouldBe 3310
      clamAvConfig.timeout shouldBe 5000
      clamAvConfig.threadPoolSize shouldBe 20
    }

    "load the ClamConfig default disabled values when enabled is set to false" in {
      val clamAvConfig = LoadClamAvConfig(configuration = configuration(false))
      clamAvConfig.chunkSize shouldBe 0
      clamAvConfig.enabled shouldBe false
      clamAvConfig.host shouldBe ""
      clamAvConfig.port shouldBe 3310
      clamAvConfig.timeout shouldBe 0
      clamAvConfig.threadPoolSize shouldBe 0
    }

    "load the ClamConfig default disabled values when enabled is missing in config" in {
      val clamAvConfig = LoadClamAvConfig(configuration = badConfiguration)
      clamAvConfig.chunkSize shouldBe 0
      clamAvConfig.enabled shouldBe false
      clamAvConfig.host shouldBe ""
      clamAvConfig.port shouldBe 3310
      clamAvConfig.timeout shouldBe 0
      clamAvConfig.threadPoolSize shouldBe 0
    }

    "throw and exception if there is no clam config setup " in {
      intercept[Exception] {
        LoadClamAvConfig(configuration = None)
      }
    }
  }
}

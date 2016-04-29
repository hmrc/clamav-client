package uk.gov.hmrc.clamav

import play.api.Configuration
import uk.gov.hmrc.clamav.config.LoadClamAvConfig
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class LoadClamAvConfigSpec extends UnitSpec with WithFakeApplication {

  def configuration(enabled: Boolean, key: String = "clam"): Configuration = {
    Configuration.from(
    Map(key ->
      Map("enabled" -> enabled , "chunkSize" -> 32768, "host" -> "avscan", "port" -> 3310, "timeout" -> 5000, "threadPoolSize" -> 20))
    )
  }

  "Test the LoadClamAvConfig" should {
    "load the ClamConfig from the play application config if present" in {
      val clamAvConfig = LoadClamAvConfig("clam", configuration = configuration(true))
      clamAvConfig.chunkSize shouldBe 32768
      clamAvConfig.enabled shouldBe true
      clamAvConfig.host shouldBe "avscan"
      clamAvConfig.port shouldBe 3310
      clamAvConfig.timeout shouldBe 5000
      clamAvConfig.threadPoolSize shouldBe 20
    }

    "throw and exception if there is no clam config setup " in {
      intercept[Exception] {
        LoadClamAvConfig("clam", configuration = configuration(false, "incorrect-key"))
      }
    }
  }
}

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

package uk.gov.hmrc.clamav.config

import play.api.Configuration

trait ClamAvConfig {
  val chunkSize: Int = 0
  val host: String = ""
  val port: Int = 0
  val timeout: Int = 0
  val threadPoolSize: Int = 0
  val maxLength: Int = 0
}

case class EnabledConfig(override val chunkSize: Int = 32768,
                         override val host: String = "localhost",
                         override val port: Int = 3310,
                         override val timeout: Int = 5000,
                         override val threadPoolSize: Int = 20,
                         override val maxLength: Int = 10485760) extends ClamAvConfig

case object DisabledConfig extends ClamAvConfig

object ClamAvConfig {
  val instream = "zINSTREAM\u0000"
  val ping = "zPING\u0000"
  val status = "nSTATS\n"
  val okClamAvResponse = "stream: OK\u0000"

  def apply(configuration: Option[Configuration]): ClamAvConfig = {
    configuration.map { c =>
      val enabled = c.getBoolean("enabled").getOrElse(false)

      if (enabled) {
        EnabledConfig(
          chunkSize = c.getInt("chunkSize").getOrElse(32768),
          host = c.getString("host").getOrElse("localhost"),
          port = c.getInt("port").getOrElse(3310),
          timeout = c.getInt("timeout").getOrElse(5000),
          threadPoolSize = c.getInt("threadPoolSize").getOrElse(20),
          maxLength = c.getInt("maxLength").getOrElse(10485760)
        )
      } else {
        DisabledConfig
      }

    }
  }.getOrElse(throw new Exception("Missing clamav configuration"))
}

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

case class ClamAvConfig(chunkSize: Int = 32768,
                        host: String = "localhost",
                        port: Int = 3310,
                        timeout: Int = 5000,
                        threadPoolSize: Int = 20,
                        maxLength: Int = 10485760)


object ClamAvConfig {

  def apply(configuration: Option[Configuration]): ClamAvConfig = {
    configuration.map { c =>
      ClamAvConfig(
        chunkSize = c.getInt("chunkSize").getOrElse(32768),
        host = c.getString("host").getOrElse("localhost"),
        port = c.getInt("port").getOrElse(3310),
        timeout = c.getInt("timeout").getOrElse(5000),
        threadPoolSize = c.getInt("threadPoolSize").getOrElse(20),
        maxLength = c.getInt("maxLength").getOrElse(10485760)
      )
    }
  }.getOrElse(throw new Exception("Missing clamav configuration"))
}

/*
 * Copyright 2020 HM Revenue & Customs
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

import javax.inject.Inject

import play.api.Configuration

trait ClamAvConfig {
  val host: String
  val port: Int
  val timeout: Int
}

class PlayClamAvConfig @Inject()(configuration: Configuration) extends ClamAvConfig {
  val host: String = getRequired(configuration.getString(_), "clam.antivirus.host")
  val port: Int = getRequired(configuration.getInt, "clam.antivirus.port")
  val timeout: Int = getRequired(configuration.getInt, "clam.antivirus.timeout")

  def getRequired[T](function: String => Option[T], key: String) =
    function(key).getOrElse(throw new IllegalStateException(s"$key missing"))
}
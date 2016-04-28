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
case class ClamAvConfig(enabled : Boolean,
                            chunkSize : Int,
                            host : String,
                            port : Int,
                            timeout : Int,
                            threadPoolSize : Int) {

  val instream = "zINSTREAM\u0000"
  val ping = "zPING\u0000"
  val status = "nSTATS\n"

  val okClamAvResponse = "stream: OK"
  val test = "test"
}

object ClamAvConfig {

  lazy val clamAvConfig = ClamAvConfig(true, 33768, "avscan", 3310, 5000, 20)
}

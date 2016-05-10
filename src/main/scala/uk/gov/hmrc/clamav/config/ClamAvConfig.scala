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

import play.api.{Configuration, Logger}

case class ClamAvConfig(enabled : Boolean,
                        chunkSize : Int,
                        host : String,
                        port : Int,
                        timeout : Int,
                        threadPoolSize : Int,
                        maxLength : Int) {

  val instream = "zINSTREAM\u0000"
  val ping = "zPING\u0000"
  val status = "nSTATS\n"

  val okClamAvResponse = "stream: OK"

}

trait ClamAvSocket {

  def openSocket(implicit clamAvConfig: ClamAvConfig) = {
    import java.net.{InetSocketAddress, Socket}
    val sock = new Socket
    sock.setSoTimeout(clamAvConfig.timeout)
    val address: InetSocketAddress = new InetSocketAddress(clamAvConfig.host, clamAvConfig.port)
    Logger.debug(s"Attempting connection to : $address")
    sock.connect(address)
    sock
  }

}


object LoadClamAvConfig {

  def apply(configuration: Option[Configuration]): ClamAvConfig = {
    configuration.map { c =>
      val enabled = c.getBoolean("enabled").getOrElse(false)

      if(enabled) {
        ClamAvConfig(
          enabled = enabled,
          chunkSize = c.getInt("chunkSize").getOrElse(32768),
          host = c.getString("host").getOrElse("localhost"),
          port = c.getInt("port").getOrElse(3310),
          timeout = c.getInt("timeout").getOrElse(5000),
          threadPoolSize = c.getInt("threadPoolSize").getOrElse(20),
          maxLength = c.getInt("maxLength").getOrElse(10485760)
        )
      } else {
        ClamAvConfig(enabled = false, chunkSize = 0, host = "", port = 3310, timeout = 0, threadPoolSize = 0, maxLength = 0 )
      }

    }
  }.getOrElse(throw new Exception("Missing clamav configuration"))

}

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

package uk.gov.hmrc.clamav

import java.io.DataOutputStream
import javax.inject.Inject

import play.api.Logger
import uk.gov.hmrc.clamav.config.ClamAvConfig

class ClamAvSocket @Inject()(config: ClamAvConfig) {
  private val instream = "zINSTREAM\u0000"

  lazy val socket = openSocket()

  lazy val toClam = {
    val ds = new DataOutputStream(socket.getOutputStream)
    ds.write(instream.getBytes())
    ds
  }

  lazy val fromClam = socket.getInputStream

  def openSocket() = {
    import java.net.{InetSocketAddress, Socket}
    val sock = new Socket
    sock.setSoTimeout(config.timeout)
    val address: InetSocketAddress = new InetSocketAddress(config.host, config.port)
    Logger.debug(s"Attempting connection to : $address")
    sock.connect(address)
    sock
  }
}

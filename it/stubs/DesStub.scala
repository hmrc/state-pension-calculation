/*
 * Copyright 2023 HM Revenue & Customs
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

package stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.JsValue
import support.WireMockMethods

object DesStub extends WireMockMethods {

  private val initialCalcUri: String = s"/individuals/pensions/ltb-calculation/initial/.*"
  private val finalCalcUri: String   = s"/individuals/pensions/ltb-calculation/final/.*"

  def initialCalc(status: Int, response: JsValue): StubMapping =
    when(method = POST, uri = initialCalcUri)
      .thenReturn(status, response)

  def finalCalc(status: Int, response: JsValue): StubMapping =
    when(method = POST, uri = finalCalcUri)
      .thenReturn(status, response)

}

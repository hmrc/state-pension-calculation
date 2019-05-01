/*
 * Copyright 2019 HM Revenue & Customs
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

package endpoints

import play.api.http.Status
import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationSpec

class ResponseTestISpec extends IntegrationSpec {

  private trait Test {

    def response(status: Int): WSResponse = {
      val request: WSRequest = buildRequest(s"/error/$status")
      await(request.get())
    }
  }

  "Calling the /error/400 endpoint" should {
    "return a 400 status code" in new Test {
      response(Status.BAD_REQUEST).status shouldBe Status.BAD_REQUEST
    }
  }

}

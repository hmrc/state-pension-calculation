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

class ApiDefinitionISpec extends IntegrationSpec {

  private trait Test {

    def response(): WSResponse = {
      val request: WSRequest = buildRequest(s"/api/definition")
      await(request.get())
    }
  }

  "Calling the /api/definition endpoint" should {

    "return a 200 status code" in new Test {
      response().status shouldBe Status.OK
    }

    "return a JSON response" in new Test {
      response().contentType shouldBe "application/json"
    }

    "return a JSON document that describes the API access" in new Test {
      private val expectedJson = Json.parse(
        """
          |{
          |  "scopes": [
          |    {
          |      "key": "read:state-pension-calculation",
          |      "name": "Get State Pension calculation",
          |      "description": "Get State Pension calculation information"
          |    },
          |    {
          |      "key": "write:state-pension-calculation",
          |      "name": "Finalise State Pension calculation",
          |      "description": "Finalise State Pension calculation information"
          |    }
          |  ],
          |  "api": {
          |    "name": "Get State Pension calculation",
          |    "description": "Get your State Pension calculation",
          |    "context": "state-pension-calculation",
          |    "versions": [
          |      {
          |        "version": "1.0",
          |        "status": "ALPHA",
          |        "endpointsEnabled": false,
          |        "access" : "PRIVATE"
          |      }
          |    ]
          |  }
          |}
        """.stripMargin
      )

      response().json shouldBe expectedJson
    }

  }

}

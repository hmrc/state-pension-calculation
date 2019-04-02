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

package connectors.httpParsers

import models.errors._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse

class HttpParserSpec extends HttpParserBaseSpec {

  "parsing JSON that represents as single DES error" should {
    "return a single error" in {
      val errorResponseJson = Json.parse(
        """
          |{
          |  "code": "TEST_CODE",
          |  "reason": "some reason"
          |}
        """.stripMargin)

      val expected = Errors(Error("TEST_CODE", "some reason"))

      val httpResponse = HttpResponse(BAD_REQUEST, Some(errorResponseJson))
      val target = new HttpParser {}
      val result = target.parseErrors(httpResponse)
      result shouldBe expected
    }
  }

  "parsing JSON that represents as multiple DES errors" should {
    "return a multiple error" in {
      val errorResponseJson = Json.parse(
        """
          |{
          |  "failures": [
          |    {
          |      "code": "TEST_CODE_1",
          |      "reason": "some reason"
          |    },
          |    {
          |      "code": "TEST_CODE_2",
          |      "reason": "some reason"
          |    }
          |  ]
          |}
        """.stripMargin)

      val expected = Errors(
        Seq(
          Error("TEST_CODE_1", "some reason"),
          Error("TEST_CODE_2", "some reason")
        )
      )

      val httpResponse = HttpResponse(BAD_REQUEST, Some(errorResponseJson))
      val target = new HttpParser {}
      val result = target.parseErrors(httpResponse)
      result shouldBe expected
    }
  }

  "parsing JSON that represents does not conform to the DES error schema" should {
    "return a internal server error" in {
      val errorResponseJson = Json.parse(
        """
          |{
          |  "error": "will robinson"
          |}
        """.stripMargin)

      val expected = Errors(InternalServerError)

      val httpResponse = HttpResponse(BAD_REQUEST, Some(errorResponseJson))
      val target = new HttpParser {}
      val result = target.parseErrors(httpResponse)
      result shouldBe expected
    }
  }

  "attempting to parse a response with no content" should {
    "return a internal server error" in {
      val expected = Errors(InternalServerError)

      val httpResponse = HttpResponse(NO_CONTENT, None)
      val target = new HttpParser {}
      val result = target.parseErrors(httpResponse)
      result shouldBe expected
    }
  }

}

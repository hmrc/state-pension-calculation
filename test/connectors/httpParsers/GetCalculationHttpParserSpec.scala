/*
 * Copyright 2024 HM Revenue & Customs
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

import connectors.httpParsers.GetCalculationHttpParser.getCalculationHttpReads
import support.data.CalculationTestData.Response._
import models.errors._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpResponse

class GetCalculationHttpParserSpec extends HttpParserBaseSpec {

  "parsing a 201 (Created) initial calc response with valid JSON" should {
    "return a valid calculation response" in {
      val httpResponse = HttpResponse(CREATED, initialCalcJson.toString())
      val result = getCalculationHttpReads.read(POST, "/test", httpResponse)

      result shouldBe Right(expectedModel)
    }
  }

  "parsing a 201 (Created) final calc response with valid JSON" should {
    "return a valid calculation response" in {
      val httpResponse = HttpResponse(CREATED, finalCalcJson.toString())
      val result = getCalculationHttpReads.read(POST, "/test", httpResponse)

      result shouldBe Right(expectedModel)
    }
  }

  "parsing a 201 (Created) response with invalid JSON" should {
    "return a single error" in {
      val invalidJson = Json.obj()
      val httpResponse = HttpResponse(CREATED, invalidJson.toString())
      val result = getCalculationHttpReads.read(POST, "/test", httpResponse)

      result shouldBe Left(Errors(ApiServiceError))
    }
  }

  "parsing a Too Many Requests (429) response" should {
    "return a single ThrottledError error" in {
      val expected = Errors(ThrottledError)

      val httpResponse = HttpResponse(TOO_MANY_REQUESTS, "")
      val result = getCalculationHttpReads.read(POST, "/test", httpResponse)
      result shouldBe Left(expected)
    }
  }

  "parsing a 502 response" should {
    "return something" in {
      val htmlResponse = "<html>oops, 502</html"
      val expected = Errors(Seq(Error(BAD_GATEWAY.toString, htmlResponse)))
      val httpResponse = HttpResponse(BAD_GATEWAY, htmlResponse)
      val result = getCalculationHttpReads.read(POST, "/test", httpResponse)
      result shouldBe Left(expected)
    }
  }

  "parsing a Service Unavailable (503) response with a throttling body" should {
    "return a single ThrottledError error" in {
      val expected = Errors(ThrottledError)
      val throttlingBody: JsValue = Json.obj("incidentReference" -> "LTM000503")

      val httpResponse = HttpResponse(SERVICE_UNAVAILABLE, throttlingBody.toString())
      val result = getCalculationHttpReads.read(POST, "/test", httpResponse)
      result shouldBe Left(expected)
    }
  }

  "parsing a failure response with a single error" should {
    "return a single error" in {
      val errorResponseJson = Json.parse(
        """
          |{
          |  "code": "TEST_CODE",
          |  "reason": "some reason"
          |}
        """.stripMargin)
      val expected = Errors(Error("TEST_CODE", "some reason"))

      val httpResponse = HttpResponse(BAD_REQUEST, errorResponseJson.toString())
      val result = getCalculationHttpReads.read(POST, "/test", httpResponse)
      result shouldBe Left(expected)
    }
  }

  "parsing a failure response with a multiple errors" should {
    "return multiple errors" in {
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

      val httpResponse = HttpResponse(BAD_REQUEST, errorResponseJson.toString())
      val result = getCalculationHttpReads.read(POST, "/test", httpResponse)
      result shouldBe Left(expected)
    }
  }

}

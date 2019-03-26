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

import connectors.httpParsers.GetCalculationHttpParser.getCalculationHttpReads
import support.data.CalculationTestData.Response._
import models.errors.{InternalServerError, SingleError}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse

class GetCalculationHttpParserSpec extends HttpParserBaseSpec {

  "parsing a 201 (Created) response with valid JSON" should {
    "return a valid calculation response" in {
      val httpResponse = HttpResponse(CREATED, Some(Json.parse(initialCalcJson)))
      val result = getCalculationHttpReads.read(POST, "/test", httpResponse)

      result shouldBe Right(expectedModel)
    }
  }

  "parsing a 201 (Created) response with invalid JSON" should {
    "return a single error" in {
      val invalidJson = Json.obj()
      val httpResponse = HttpResponse(CREATED, Some(invalidJson))
      val result = getCalculationHttpReads.read(POST, "/test", httpResponse)

      result shouldBe Left(SingleError(InternalServerError))
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
      val expected = SingleError(InternalServerError)

      val httpResponse = HttpResponse(BAD_REQUEST, Some(errorResponseJson))
      val result = getCalculationHttpReads.read(POST, "/test", httpResponse)
      result shouldBe Left(expected)
    }

  }
}

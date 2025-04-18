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

package endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.errors._
import play.api.http.{HeaderNames, Status}
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import stubs.{AuditStub, DesStub}
import support.IntegrationSpec
import support.data.CalculationTestData.{Response => testData}
import utils.AdditionalHeaderNames.CorrelationIdHeader
import utils.ErrorCodes._

import java.util.UUID

class CalculationISpec extends IntegrationSpec {

  private trait Test {

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(s"/calculation")
        .addHttpHeaders(CorrelationIdHeader -> UUID.randomUUID().toString)
    }

  }

  "Calling the /calculation endpoint" when {

    "the request is valid initial calc" should {

      trait InitialCalcTest extends Test {
        lazy val desResponse: JsValue = testData.initialCalcJson
        lazy val requestBody: JsValue = Json.obj(
          "nino"       -> "AA123456A",
          "checkBrick" -> "SMIJ",
          "gender"     -> "M",
          "finalise"   -> false
        )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          DesStub.initialCalc(Status.CREATED, desResponse)
        }

        lazy val response: WSResponse = await(request().post(requestBody))
      }

      "return a 201 status code" in new InitialCalcTest {
        response.status shouldBe Status.CREATED
      }

      "return the correct JSON" in new InitialCalcTest {
        response.body[JsValue] shouldBe testData.generatedJson
      }

      "have the correct Content-Type header and value" in new InitialCalcTest {
        response.header(HeaderNames.CONTENT_TYPE) shouldBe Some("application/json")
      }

    }

    "the request is valid final calc" should {

      trait FinalCalcTest extends Test {
        lazy val desResponse: JsValue = testData.finalCalcJson
        lazy val requestBody: JsValue = Json.obj(
          "nino"       -> "AA123456A",
          "checkBrick" -> "SMIJ",
          "gender"     -> "M",
          "finalise"   -> true
        )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          DesStub.finalCalc(Status.CREATED, desResponse)
        }

        lazy val response: WSResponse = await(request().post(requestBody))
      }

      "return a 201 status code" in new FinalCalcTest {
        response.status shouldBe Status.CREATED
      }

      "return the correct JSON" in new FinalCalcTest {
        response.body[JsValue] shouldBe testData.generatedJson
      }

      "have the correct Content-Type header and value" in new FinalCalcTest {
        response.header(HeaderNames.CONTENT_TYPE) shouldBe Some("application/json")
      }

    }

    def testDesErrorHandling(
        desErrorCode: String,
        desStatusCode: Int,
        desResponseBody: JsValue,
        apiStatusCode: Int,
        apiResponseBody: JsValue
    ): Unit =

      s"DES responds with $desErrorCode" should {

        trait CalcTest extends Test {
          lazy val desResponse: JsValue = desResponseBody
          lazy val requestBody: JsValue = Json.obj(
            "nino"       -> "AA123456A",
            "checkBrick" -> "SMIJ",
            "gender"     -> "M",
            "finalise"   -> true
          )

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            DesStub.finalCalc(desStatusCode, desResponse)
          }

          lazy val response: WSResponse = await(request().post(requestBody))
        }

        s"return a $apiStatusCode status code" in new CalcTest {
          response.status shouldBe apiStatusCode
        }

        "return the correct JSON" in new CalcTest {
          response.body[JsValue] shouldBe apiResponseBody
        }

        "have the correct Content-Type header and value" in new CalcTest {
          response.header(HeaderNames.CONTENT_TYPE) shouldBe Some("application/json")
        }

      }

    {
      val invalidPayloadBody = Json.obj(
        "code"   -> InvalidPayloadCode,
        "reason" -> "Submission has not passed validation. Invalid Payload."
      )

      testDesErrorHandling(
        InvalidPayloadCode,
        Status.BAD_REQUEST,
        invalidPayloadBody,
        Status.INTERNAL_SERVER_ERROR,
        Json.toJson[Error](ApiServiceError)
      )
    }

    {
      val invalidNinoBody = Json.obj(
        "code"   -> InvalidNinoCode,
        "reason" -> "Submission has not passed validation. Invalid parameter nino."
      )

      testDesErrorHandling(
        InvalidNinoCode,
        Status.BAD_REQUEST,
        invalidNinoBody,
        Status.INTERNAL_SERVER_ERROR,
        Json.toJson[Error](ApiServiceError)
      )
    }

    {
      val invalidCorrelationIdBody = Json.obj(
        "code"   -> InvalidCorrelationIdCode,
        "reason" -> "Submission has not passed validation. Invalid header CorrelationId."
      )

      testDesErrorHandling(
        InvalidCorrelationIdCode,
        Status.BAD_REQUEST,
        invalidCorrelationIdBody,
        Status.INTERNAL_SERVER_ERROR,
        Json.toJson[Error](ApiServiceError)
      )
    }

    {
      val multipleErrorCodes = s"$InvalidPayloadCode and $InvalidNinoCode"
      val invalidBody = Json.obj(
        "failures" -> Json.arr(
          Json.obj(
            "code"   -> InvalidPayloadCode,
            "reason" -> "Submission has not passed validation. Invalid Payload."
          ),
          Json.obj(
            "code"   -> InvalidNinoCode,
            "reason" -> "Submission has not passed validation. Invalid parameter nino."
          )
        )
      )

      testDesErrorHandling(
        multipleErrorCodes,
        Status.BAD_REQUEST,
        invalidBody,
        Status.INTERNAL_SERVER_ERROR,
        Json.toJson[Error](ApiServiceError)
      )
    }

    {
      val invalidBody = Json.obj(
        "code"   -> RetirementDateAfterDeathDateCode,
        "reason" -> "The remote endpoint has indicated that the Date of Retirement is after the Date of Death."
      )

      testDesErrorHandling(
        RetirementDateAfterDeathDateCode,
        Status.BAD_REQUEST,
        invalidBody,
        Status.FORBIDDEN,
        Json.toJson[Error](RetirementAfterDeathError)
      )
    }

    {
      val invalidBody = Json.obj(
        "code" -> TooEarlyCode,
        "reason" -> "The remote endpoint has indicated that the pension calculation can only be done within 6 months of the SPA date."
      )

      testDesErrorHandling(
        TooEarlyCode,
        Status.BAD_REQUEST,
        invalidBody,
        Status.FORBIDDEN,
        Json.toJson[Error](TooEarlyError)
      )
    }

    {
      val invalidBody = Json.obj(
        "code"   -> UnknownBusinessErrorCode,
        "reason" -> "The remote endpoint has returned an unknown business validation error."
      )

      testDesErrorHandling(
        UnknownBusinessErrorCode,
        Status.BAD_REQUEST,
        invalidBody,
        Status.FORBIDDEN,
        Json.toJson[Error](UnknownBusinessError)
      )
    }

    {
      val invalidBody = Json.obj(
        "code"   -> NinoNotFoundCode,
        "reason" -> "The remote endpoint has indicated that the Nino provided cannot be found."
      )

      testDesErrorHandling(
        NinoNotFoundCode,
        Status.NOT_FOUND,
        invalidBody,
        Status.NOT_FOUND,
        Json.toJson[Error](NinoNotFoundError)
      )
    }

    {
      val invalidBody = Json.obj(
        "code"   -> MatchNotFoundCode,
        "reason" -> "The remote endpoint has indicated that there is no match for the person details provided."
      )

      testDesErrorHandling(
        MatchNotFoundCode,
        Status.NOT_FOUND,
        invalidBody,
        Status.NOT_FOUND,
        Json.toJson[Error](MatchNotFoundError)
      )
    }

    {
      val invalidBody = Json.obj(
        "code"   -> ServerErrorCode,
        "reason" -> "DES is currently experiencing problems that require live service intervention."
      )

      testDesErrorHandling(
        ServerErrorCode,
        Status.INTERNAL_SERVER_ERROR,
        invalidBody,
        Status.INTERNAL_SERVER_ERROR,
        Json.toJson[Error](ApiServiceError)
      )
    }

    {
      val invalidBody = Json.obj(
        "code"   -> ServiceUnavailableCode,
        "reason" -> "Dependent systems are currently not responding."
      )

      testDesErrorHandling(
        ServiceUnavailableCode,
        Status.SERVICE_UNAVAILABLE,
        invalidBody,
        Status.SERVICE_UNAVAILABLE,
        Json.toJson[Error](ServiceUnavailableError)
      )
    }

    "DES responds with 429 response code" should {

      trait CalcTest extends Test {
        lazy val requestBody: JsValue = Json.obj(
          "nino"       -> "AA123456A",
          "checkBrick" -> "SMIJ",
          "gender"     -> "M",
          "finalise"   -> true
        )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          DesStub.finalCalc(Status.TOO_MANY_REQUESTS, Json.obj())
        }

        lazy val response: WSResponse = await(request().post(requestBody))
      }

      s"return a 429 status code" in new CalcTest {
        response.status shouldBe Status.TOO_MANY_REQUESTS
      }

      "return the correct JSON" in new CalcTest {
        response.body[JsValue] shouldBe Json.toJson[Error](ThrottledError)
      }

      "have the correct Content-Type header and value" in new CalcTest {
        response.header(HeaderNames.CONTENT_TYPE) shouldBe Some("application/json")
      }

    }

    {
      val serviceUnavailableCode = "SERVICE_UNAVAILABLE throttled response"
      val invalidBody            = Json.obj("incidentReference" -> "LTM000503")

      testDesErrorHandling(
        serviceUnavailableCode,
        Status.SERVICE_UNAVAILABLE,
        invalidBody,
        Status.TOO_MANY_REQUESTS,
        Json.toJson[Error](ThrottledError)
      )
    }

    {
      val serverErrorCode = "a calculation error"
      val invalidBody = Json.obj(
        "code"   -> "12345",
        "reason" -> "Backend returned calculation error code 12345."
      )
      val expectedResponse =
        Error(CalculationErrorCodePrefix + "12345", "Backend returned calculation error code 12345.")

      testDesErrorHandling(
        serverErrorCode,
        Status.CONFLICT,
        invalidBody,
        Status.FORBIDDEN,
        Json.toJson(expectedResponse)
      )
    }

    {
      val serverErrorCode = "multiple calculation errors"
      val invalidBody = Json.obj(
        "failures" -> Json.arr(
          Json.obj(
            "code"   -> "12345",
            "reason" -> "Backend returned calculation error code 12345."
          ),
          Json.obj(
            "code"   -> "123456",
            "reason" -> "Backend returned calculation error code 123456."
          )
        )
      )
      val expectedResponse = Errors(
        Seq(
          Error(CalculationErrorCodePrefix + "12345", "Backend returned calculation error code 12345."),
          Error(CalculationErrorCodePrefix + "123456", "Backend returned calculation error code 123456.")
        )
      )
      testDesErrorHandling(
        serverErrorCode,
        Status.CONFLICT,
        invalidBody,
        Status.FORBIDDEN,
        Json.toJson(expectedResponse)
      )
    }

  }

}

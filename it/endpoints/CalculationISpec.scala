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

import java.util.UUID

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.errors._
import play.api.http.{HeaderNames, Status}
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import stubs.{AuditStub, DesStub}
import support.IntegrationSpec
import support.data.CalculationTestData.{Response => testData}
import utils.AdditionalHeaderNames.CorrelationIdHeader

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
          "nino" -> "AA123456A",
          "checkBrick" -> "SMIJ",
          "gender" -> "M",
          "finalise" -> false
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
          "nino" -> "AA123456A",
          "checkBrick" -> "SMIJ",
          "gender" -> "M",
          "finalise" -> true
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

    def testDesErrorHandling(desErrorCode: String,
                             desStatusCode: Int,
                             desResponseBody: JsValue,
                             apiStatusCode: Int,
                             apiResponseBody: JsValue): Unit = {

      s"DES responds with $desErrorCode" should {

        trait CalcTest extends Test {
          lazy val desResponse: JsValue = desResponseBody
          lazy val requestBody: JsValue = Json.obj(
            "nino" -> "AA123456A",
            "checkBrick" -> "SMIJ",
            "gender" -> "M",
            "finalise" -> true
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
    }

    {
      val invalidPayloadErrorCode = "INVALID_PAYLOAD"
      val invalidPayloadBody = Json.obj(
        "code" -> invalidPayloadErrorCode,
        "reason" -> "Submission has not passed validation. Invalid Payload."
      )

      testDesErrorHandling(invalidPayloadErrorCode,
        Status.BAD_REQUEST,
        invalidPayloadBody,
        Status.INTERNAL_SERVER_ERROR,
        Json.toJson(ApiServiceError))
    }

    {
      val invalidNinoErrorCode = "INVALID_NINO"
      val invalidNinoBody = Json.obj(
        "code" -> invalidNinoErrorCode,
        "reason" -> "Submission has not passed validation. Invalid parameter nino."
      )

      testDesErrorHandling(invalidNinoErrorCode,
        Status.BAD_REQUEST,
        invalidNinoBody,
        Status.INTERNAL_SERVER_ERROR,
        Json.toJson(ApiServiceError))
    }

    {
      val invalidCorrelationIdErrorCode = "INVALID_CORRELATIONID"
      val invalidCorrelationIdBody = Json.obj(
        "code" -> invalidCorrelationIdErrorCode,
        "reason" -> "Submission has not passed validation. Invalid header CorrelationId."
      )

      testDesErrorHandling(invalidCorrelationIdErrorCode,
        Status.BAD_REQUEST,
        invalidCorrelationIdBody,
        Status.INTERNAL_SERVER_ERROR,
        Json.toJson(ApiServiceError))
    }

    {
      val multipleErrorCodes = "INVALID_PAYLOAD and INVALID_NINO"
      val invalidBody = Json.obj(
        "failures" -> Json.arr(
          Json.obj(
            "code" -> "INVALID_PAYLOAD",
            "reason" -> "Submission has not passed validation. Invalid Payload."
          ),
          Json.obj(
            "code" -> "INVALID_NINO",
            "reason" -> "Submission has not passed validation. Invalid parameter nino."
          )
        )
      )

      testDesErrorHandling(multipleErrorCodes,
        Status.BAD_REQUEST,
        invalidBody,
        Status.INTERNAL_SERVER_ERROR,
        Json.toJson(ApiServiceError))
    }

    {
      val retirementAfterDeathCode = "RETIREMENT_DATE_AFTER_DEATH_DATE"
      val invalidBody = Json.obj(
        "code" -> retirementAfterDeathCode,
        "reason" -> "The remote endpoint has indicated that the Date of Retirement is after the Date of Death."
      )

      testDesErrorHandling(retirementAfterDeathCode,
        Status.BAD_REQUEST,
        invalidBody,
        Status.FORBIDDEN,
        Json.toJson(RetirementAfterDeathError))
    }

    {
      val tooEarlyCode = "TOO_EARLY"
      val invalidBody = Json.obj(
        "code" -> tooEarlyCode,
        "reason" -> "The remote endpoint has indicated that the pension calculation can only be done within 6 months of the SPA date."
      )

      testDesErrorHandling(tooEarlyCode,
        Status.BAD_REQUEST,
        invalidBody,
        Status.FORBIDDEN,
        Json.toJson(TooEarlyError))
    }

    {
      val unknownBusinessErrorCode = "UNKNOWN_BUSINESS_ERROR"
      val invalidBody = Json.obj(
        "code" -> unknownBusinessErrorCode,
        "reason" -> "The remote endpoint has returned an unknown business validation error."
      )

      testDesErrorHandling(unknownBusinessErrorCode,
        Status.BAD_REQUEST,
        invalidBody,
        Status.FORBIDDEN,
        Json.toJson(UnknownBusinessError))
    }

    {
      val notFoundCode = "NOT_FOUND_NINO"
      val invalidBody = Json.obj(
        "code" -> notFoundCode,
        "reason" -> "The remote endpoint has indicated that the Nino provided cannot be found."
      )

      testDesErrorHandling(notFoundCode,
        Status.NOT_FOUND,
        invalidBody,
        Status.NOT_FOUND,
        Json.toJson(NinoNotFoundError))
    }

    {
      val notFoundCode = "NO_MATCH_FOUND"
      val invalidBody = Json.obj(
        "code" -> notFoundCode,
        "reason" -> "The remote endpoint has indicated that there is no match for the person details provided."
      )

      testDesErrorHandling(notFoundCode,
        Status.NOT_FOUND,
        invalidBody,
        Status.NOT_FOUND,
        Json.toJson(MatchNotFoundError))
    }

    {
      val serverErrorCode = "SERVER_ERROR"
      val invalidBody = Json.obj(
        "code" -> serverErrorCode,
        "reason" -> "DES is currently experiencing problems that require live service intervention."
      )

      testDesErrorHandling(serverErrorCode,
        Status.INTERNAL_SERVER_ERROR,
        invalidBody,
        Status.INTERNAL_SERVER_ERROR,
        Json.toJson(ApiServiceError))
    }

    {
      val serviceUnavailableCode = "SERVICE_UNAVAILABLE"
      val invalidBody = Json.obj(
        "code" -> serviceUnavailableCode,
        "reason" -> "Dependent systems are currently not responding."
      )

      testDesErrorHandling(serviceUnavailableCode,
        Status.SERVICE_UNAVAILABLE,
        invalidBody,
        Status.SERVICE_UNAVAILABLE,
        Json.toJson(ServiceUnavailableError))
    }

    "DES responds with 429 response code" should {

      trait CalcTest extends Test {
        lazy val requestBody: JsValue = Json.obj(
          "nino" -> "AA123456A",
          "checkBrick" -> "SMIJ",
          "gender" -> "M",
          "finalise" -> true
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
        response.body[JsValue] shouldBe Json.toJson(ThrottledError)
      }

      "have the correct Content-Type header and value" in new CalcTest {
        response.header(HeaderNames.CONTENT_TYPE) shouldBe Some("application/json")
      }

    }

    {
      val serverErrorCode = "a calculation error"
      val invalidBody = Json.obj(
        "code" -> "12345",
        "reason" -> "Backend returned calculation error code 12345."
      )
      val expectedResponse = Error("12345", "Backend returned calculation error code 12345.")

      testDesErrorHandling(serverErrorCode,
        Status.CONFLICT,
        invalidBody,
        Status.FORBIDDEN,
        Json.toJson(expectedResponse))
    }

    {
      val serverErrorCode = "multiple calculation errors"
      val invalidBody = Json.obj(
        "failures" -> Json.arr(
          Json.obj(
            "code" -> "12345",
            "reason" -> "Backend returned calculation error code 12345."
          ),
          Json.obj(
            "code" -> "123456",
            "reason" -> "Backend returned calculation error code 123456."
          )
        )
      )
      val expectedResponse = Errors(
        Seq(
          Error("12345", "Backend returned calculation error code 12345."),
          Error("123456", "Backend returned calculation error code 123456.")
        )
      )
      testDesErrorHandling(serverErrorCode,
        Status.CONFLICT,
        invalidBody,
        Status.FORBIDDEN,
        Json.toJson(expectedResponse))
    }

  }

}

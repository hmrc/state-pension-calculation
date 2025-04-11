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

package controllers

import mocks.MockCalculationService
import models.CalculationRequest
import models.errors._
import play.api.http.Status
import play.api.libs.json.Json.toJson
import play.api.libs.json.{JsObject, JsValue, Json, Writes}
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.stubControllerComponents
import support.data.CalculationTestData.Response.{expectedModel => validResponse}
import utils.AdditionalHeaderNames.CorrelationIdHeader
import utils.ErrorCodes.CalculationErrorCodePrefix

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CalculationControllerSpec extends ControllerBaseSpec {

  private trait Test extends MockCalculationService {
    lazy val target = new CalculationController(stubControllerComponents(), mockCalculationService)
  }

  val InvalidRequestErrorJson: JsValue = Json.parse(
    s"""
       |{
       |  "code": "INVALID_REQUEST",
       |  "message": "The request is invalid."
       |}
      """.stripMargin
  )

  val UnexpectedFryAmountErrorJson: JsValue = Json.parse(
    s"""
       |{
       |  "code": "FRY_AMOUNT_NOT_EXPECTED",
       |  "message": "Do not supply a FRY amount for an initial calculation."
       |}
      """.stripMargin
  )

  val validPayload: JsObject = Json.obj(
    "nino"       -> "AA123456A",
    "checkBrick" -> "SMIJ",
    "gender"     -> "M",
    "finalise"   -> true
  )

  implicit val request: Request[JsValue] = postRequest[JsValue](validPayload)

  val calcRequest = CalculationRequest("AA123456A", "M", "SMIJ", finalCalculation = true, correlationId = correlationId)

  "Calling the calculation action" when {

    "the request is valid" should {

      "return 201" in new Test {

        MockedCalculationService
          .calculate(calcRequest)
          .returns(Future.successful(Right(validResponse)))

        private val result = target.calculation()(request)
        status(result) shouldBe Status.CREATED
      }

      "return the Correlation ID in the header" in new Test {

        MockedCalculationService
          .calculate(calcRequest)
          .returns(Future.successful(Right(validResponse)))

        private val result = target.calculation()(request)
        header(CorrelationIdHeader, result) shouldBe Some(correlationId)
      }

    }

    "the request is missing the Correlation ID header" should {
      val invalidRequest = FakeRequest().withBody(validPayload)

      "return 400" in new Test {
        private val result = target.calculation()(invalidRequest)
        status(result) shouldBe Status.BAD_REQUEST
      }

      "return an INVALID_REQUEST error in the body" in new Test {
        private val result = target.calculation()(invalidRequest)
        contentAsJson(result) shouldBe InvalidRequestErrorJson
      }

    }

    "the request contains an invalid Correlation ID header" should {
      val invalidRequest = FakeRequest()
        .withHeaders(CorrelationIdHeader -> "BAD CORRELATION ID")
        .withBody(validPayload)

      "return 400" in new Test {
        private val result = target.calculation()(invalidRequest)
        status(result) shouldBe Status.BAD_REQUEST
      }

      "return an INVALID_REQUEST error in the body" in new Test {
        private val result = target.calculation()(invalidRequest)
        contentAsJson(result) shouldBe InvalidRequestErrorJson
      }

    }

    def testMissingRequestProperty(propertyName: String) {
      s"the request is missing the $propertyName property" should {
        val invalidPayload = validPayload - propertyName
        val invalidRequest = postRequest[JsValue](invalidPayload)

        "return 400" in new Test {
          private val result = target.calculation()(invalidRequest)
          status(result) shouldBe Status.BAD_REQUEST
        }

        "return an INVALID_REQUEST error in the body" in new Test {
          private val result = target.calculation()(invalidRequest)
          contentAsJson(result) shouldBe InvalidRequestErrorJson
        }

      }
    }

    def testInvalidRequestProperty[T](
        propertyName: String,
        invalidValue: T,
        expectedError: Error = InvalidRequestError
    )(implicit w: Writes[T]) {
      s"the request has an invalid value ($invalidValue) for the property $propertyName" should {
        val invalidPayload = validPayload ++ Json.obj(propertyName -> invalidValue)
        val invalidRequest = postRequest[JsValue](invalidPayload)

        "return 400" in new Test {
          private val result = target.calculation()(invalidRequest)
          status(result) shouldBe Status.BAD_REQUEST
        }

        "return an INVALID_REQUEST error in the body" in new Test {
          private val result = target.calculation()(invalidRequest)
          contentAsJson(result) shouldBe toJson(expectedError)
        }
      }
    }

    testMissingRequestProperty("nino")
    testInvalidRequestProperty("nino", "INVALID NINO")

    testMissingRequestProperty("checkBrick")
    testInvalidRequestProperty("checkBrick", "INVALID CHECK BRICK")

    testMissingRequestProperty("gender")
    testInvalidRequestProperty("gender", "X")

    testMissingRequestProperty("finalise")
    testInvalidRequestProperty("finalise", "maybe?")

    testInvalidRequestProperty("fryAmount", false)
    testInvalidRequestProperty("fryAmount", BigDecimal("-0.01"))
    testInvalidRequestProperty("fryAmount", BigDecimal("1000000000.00"))
    testInvalidRequestProperty("fryAmount", BigDecimal("1.123"))

    "the request has a FRY amount for an initial calc" should {
      val invalidPayload = validPayload ++
        Json.obj("fryAmount" -> BigDecimal("1.00")) ++
        Json.obj("finalise" -> false)
      val invalidRequest = postRequest[JsValue](invalidPayload)

      "return 400" in new Test {
        MockedCalculationService
          .calculate(calcRequest)
          .returns(Future.successful(Right(validResponse)))

        private val result = target.calculation()(invalidRequest)

        status(result) shouldBe Status.BAD_REQUEST
      }

      "return an FRY_AMOUNT_NOT_EXPECTED error in the body" in new Test {
        MockedCalculationService
          .calculate(calcRequest)
          .returns(Future.successful(Right(validResponse)))

        private val result = target.calculation()(invalidRequest)

        contentAsJson(result) shouldBe UnexpectedFryAmountErrorJson
      }
    }

    "an InternalServerError is returned from the service" should {
      "return a 500 response" in new Test {
        MockedCalculationService
          .calculate(calcRequest)
          .returns(Future.successful(Left(Errors(ApiServiceError))))

        private val result = target.calculation()(request)
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }
    }

    "an RetirementAfterDeathError is returned from the service" should {
      "return a 403 response" in new Test {
        MockedCalculationService
          .calculate(calcRequest)
          .returns(Future.successful(Left(Errors(RetirementAfterDeathError))))

        private val result = target.calculation()(request)
        status(result) shouldBe Status.FORBIDDEN
      }
    }

    "an TooEarlyError is returned from the service" should {
      "return a 403 response" in new Test {
        MockedCalculationService
          .calculate(calcRequest)
          .returns(Future.successful(Left(Errors(TooEarlyError))))

        private val result = target.calculation()(request)
        status(result) shouldBe Status.FORBIDDEN
      }
    }

    "an UnknownBusinessError is returned from the service" should {
      "return a 403 response" in new Test {
        MockedCalculationService
          .calculate(calcRequest)
          .returns(Future.successful(Left(Errors(UnknownBusinessError))))

        private val result = target.calculation()(request)
        status(result) shouldBe Status.FORBIDDEN
      }
    }

    "a NinoNotFoundError is returned from the service" should {
      "return a 404 response" in new Test {
        MockedCalculationService
          .calculate(calcRequest)
          .returns(Future.successful(Left(Errors(NinoNotFoundError))))

        private val result = target.calculation()(request)
        status(result) shouldBe Status.NOT_FOUND
      }
    }

    "a MatchNotFoundError is returned from the service" should {
      "return a 404 response" in new Test {
        MockedCalculationService
          .calculate(calcRequest)
          .returns(Future.successful(Left(Errors(MatchNotFoundError))))

        private val result = target.calculation()(request)
        status(result) shouldBe Status.NOT_FOUND
      }
    }

    "a ServiceUnavailableError is returned from the service" should {
      "return a 503 response" in new Test {
        MockedCalculationService
          .calculate(calcRequest)
          .returns(Future.successful(Left(Errors(ServiceUnavailableError))))

        private val result = target.calculation()(request)
        status(result) shouldBe Status.SERVICE_UNAVAILABLE
      }
    }

    "a ThrottledError is returned from the service" should {
      "return a 429 response" in new Test {
        MockedCalculationService
          .calculate(calcRequest)
          .returns(Future.successful(Left(Errors(ThrottledError))))

        private val result = target.calculation()(request)
        status(result) shouldBe Status.TOO_MANY_REQUESTS
      }
    }

    "a calculation error is returned from the service" should {
      "return a 403 response" in new Test {
        val calculationError = Error(CalculationErrorCodePrefix + "12345", "Calc error 12345")
        MockedCalculationService
          .calculate(calcRequest)
          .returns(Future.successful(Left(Errors(calculationError))))

        private val result = target.calculation()(request)
        status(result) shouldBe Status.FORBIDDEN
      }
    }

    "multiple calculation errors are returned from the service" should {
      "return a 403 response" in new Test {
        val calculationError1 = Error(CalculationErrorCodePrefix + "12345", "Calc error 12345")
        val calculationError2 = Error(CalculationErrorCodePrefix + "12345", "Calc error 12345")
        MockedCalculationService
          .calculate(calcRequest)
          .returns(Future.successful(Left(Errors(Seq(calculationError1, calculationError2)))))

        private val result = target.calculation()(request)
        status(result) shouldBe Status.FORBIDDEN
      }
    }
  }

}

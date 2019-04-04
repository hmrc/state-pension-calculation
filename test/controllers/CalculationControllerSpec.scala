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

package controllers

import mocks.MockCalculationService
import models.CalculationRequest
import models.errors._
import play.api.http.Status
import play.api.libs.json.Json.toJson
import play.api.libs.json.{JsObject, JsValue, Json, Writes}
import play.api.mvc.Request
import play.api.test.Helpers.stubControllerComponents
import support.data.CalculationTestData.Response.{expectedModel => validResponse}

import scala.concurrent.Future

class CalculationControllerSpec extends ControllerBaseSpec {

  private trait Test extends MockCalculationService {
    lazy val target = new CalculationController(stubControllerComponents(), mockCalculationService)
  }

  val validPayload: JsObject = Json.obj(
    "nino" -> "AA123456A",
    "checkBrick" -> "SMIJ",
    "gender" -> "M",
    "finalise" -> false
  )

  implicit val request: Request[JsValue] = fakePostRequest[JsValue](validPayload)

  val calcRequest = CalculationRequest("AA123456A", "M", "SMIJ")

  "Calling the calculation action" when {

    "the request is valid" should {

      "return 201" in new Test {

        MockedCalculationService.calculate(calcRequest)
          .returns(Future.successful(Right(validResponse)))

        private val result = target.calculation()(request)
        status(result) shouldBe Status.CREATED
      }

    }

    def testMissingRequestProperty(propertyName: String) {
      s"the request is missing the $propertyName property" should {
        val invalidPayload = validPayload - propertyName
        val invalidRequest = fakePostRequest[JsValue](invalidPayload)

        "return 400" in new Test {

          MockedCalculationService.calculate(calcRequest)
            .returns(Future.successful(Right(validResponse)))

          private val result = target.calculation()(invalidRequest)
          status(result) shouldBe Status.BAD_REQUEST
        }

        "return an INVALID_REQUEST error in the body" in new Test {
          MockedCalculationService.calculate(calcRequest)
            .returns(Future.successful(Right(validResponse)))

          private val result = target.calculation()(invalidRequest)

          contentAsJson(result) shouldBe toJson(InvalidRequestError)
        }

      }
    }

    def testInvalidRequestProperty[T](propertyName: String,
                                      invalidValue: T,
                                      expectedError: Error = InvalidRequestError)(implicit w: Writes[T]) {
      s"the request has an invalid value for the property $propertyName" should {
        val invalidPayload = validPayload ++ Json.obj(propertyName -> invalidValue)
        val invalidRequest = fakePostRequest[JsValue](invalidPayload)

        "return 400" in new Test {
          MockedCalculationService.calculate(calcRequest)
            .returns(Future.successful(Right(validResponse)))

          private val result = target.calculation()(invalidRequest)

          status(result) shouldBe Status.BAD_REQUEST
        }

        "return an INVALID_REQUEST error in the body" in new Test {
          MockedCalculationService.calculate(calcRequest)
            .returns(Future.successful(Right(validResponse)))

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

    "the request has a FRY amount for an initial calc" should {
      val invalidPayload = validPayload ++ Json.obj("fryAmount" -> BigDecimal("1.00"))
      val invalidRequest = fakePostRequest[JsValue](invalidPayload)

      "return 400" in new Test {
        MockedCalculationService.calculate(calcRequest)
          .returns(Future.successful(Right(validResponse)))

        private val result = target.calculation()(invalidRequest)

        status(result) shouldBe Status.BAD_REQUEST
      }

      "return an FRY_AMOUNT_NOT_EXPECTED error in the body" in new Test {
        MockedCalculationService.calculate(calcRequest)
          .returns(Future.successful(Right(validResponse)))

        private val result = target.calculation()(invalidRequest)

        contentAsJson(result) shouldBe toJson(UnexpectedFryAmountError)
      }
    }

    "an InternalServerError is returned from the service" should {
      "return a 500 response" in new Test {
        MockedCalculationService.calculate(calcRequest)
          .returns(Future.successful(Left(Errors(ApiServiceError))))

        private val result = target.calculation()(request)
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }
    }

    "an RetirementAfterDeathError is returned from the service" should {
      "return a 403 response" in new Test {
        MockedCalculationService.calculate(calcRequest)
          .returns(Future.successful(Left(Errors(RetirementAfterDeathError))))

        private val result = target.calculation()(request)
        status(result) shouldBe Status.FORBIDDEN
      }
    }

    "an TooEarlyError is returned from the service" should {
      "return a 403 response" in new Test {
        MockedCalculationService.calculate(calcRequest)
          .returns(Future.successful(Left(Errors(TooEarlyError))))

        private val result = target.calculation()(request)
        status(result) shouldBe Status.FORBIDDEN
      }
    }

    "an UnknownBusinessError is returned from the service" should {
      "return a 403 response" in new Test {
        MockedCalculationService.calculate(calcRequest)
          .returns(Future.successful(Left(Errors(UnknownBusinessError))))

        private val result = target.calculation()(request)
        status(result) shouldBe Status.FORBIDDEN
      }
    }
  }

}

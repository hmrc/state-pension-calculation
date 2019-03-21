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
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Request
import play.api.test.Helpers.stubControllerComponents
import support.data.CalculationTestData.Response.{expectedModel => validResponse}

import scala.concurrent.Future

class CalculationControllerSpec extends ControllerBaseSpec {

  private trait Test extends MockCalculationService {
    lazy val target = new CalculationController(stubControllerComponents(), mockCalculationService)
  }

  val payload: JsValue = Json.obj(
    "nino" -> "AA123456A",
    "checkBrick" -> "SMIJ",
    "gender" -> "M",
    "finalise" -> false
  )

  implicit val request: Request[JsValue] = fakePostRequest[JsValue](payload)

  val calcRequest = CalculationRequest("AA123456A", "M", "SMIJ")

  "Calling the calculation action" when {

    "the request is valid" should {

      "return 200" in new Test {

        MockedCalculationService.calculate(calcRequest)
          .returns(Future.successful(Right(validResponse)))

        private val result = target.calculation()(request)
        status(result) shouldBe Status.OK
      }

    }

    "the request is invalid" should {

      "return 200" in new Test {

        MockedCalculationService.calculate(calcRequest)
          .returns(Future.successful(Right(validResponse)))

        private val invalidRequest = fakePostRequest[JsValue](Json.obj())

        private val result = target.calculation()(invalidRequest)
        status(result) shouldBe Status.BAD_REQUEST
      }

    }

    "a single error is returned from the service" should {
      "not return a 200 response" in new Test {
        MockedCalculationService.calculate(calcRequest)
          .returns(Future.successful(Left(SingleError(InternalServerError))))

        private val result = target.calculation()(request)
        status(result) shouldNot be(Status.OK)
      }
    }

    "multiple errors are returned by the service" should {
      "not return a 200 response" in new Test {
        val errors = MultipleErrors(
          Seq(
            InternalServerError,
            KnownError("A", "B")
          )
        )
        MockedCalculationService.calculate(calcRequest)
          .returns(Future.successful(Left(errors)))

        private val result = target.calculation()(request)
        status(result) shouldNot be(Status.OK)
      }
    }

  }

  "Calling buildRequest" when {
    "the json is valid" should {
      "create a valid request instance" in new Test {

        private val result = target.buildRequest(payload)

        result shouldBe Right(calcRequest)

      }
    }

    "the json is invalid" should {
      "return an error" in new Test {

        private val result = target.buildRequest(Json.obj())

        result shouldBe Left(SingleError(InvalidRequestError))

      }
    }
  }

}

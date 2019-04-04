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

package services

import mocks.MockDesConnector
import models.errors._
import models.{CalculationOutcome, CalculationRequest}
import support.data.CalculationTestData.Response.{expectedModel => validResponse}

import scala.concurrent.Future

class CalculationServiceSpec extends ServiceBaseSpec {

  private trait Test extends MockDesConnector {
    lazy val target = new CalculationService(mockDesConnector)
  }

  "Calling calculate" when {

    val validRequest = CalculationRequest("AA12456A", "M", "SMIJ", finalCalculation = true)

    "the request is not final" should {

      "call the getInitialCalculation method on the connector" in new Test {
        val request = CalculationRequest("AA12456A", "M", "SMIJ")

        MockedDesConnector.getInitialCalculation(request)
          .returns(Future.successful(Right(validResponse)))

        val result: CalculationOutcome = await(target.calculate(request))
        result shouldBe Right(validResponse)
      }
    }

    "the request is final" should {
      "call the getFinalCalculation method on the connector" in new Test {
        val request = CalculationRequest("AA12456A", "M", "SMIJ", finalCalculation = true)

        MockedDesConnector.getFinalCalculation(request)
          .returns(Future.successful(Right(validResponse)))

        val result: CalculationOutcome = await(target.calculate(request))
        result shouldBe Right(validResponse)
      }
    }

    "an unknown error is returned" should {
      "convert the error to an InternalServerError" in new Test {
        val error = Error("UNKNOWN", "unknown message")
        MockedDesConnector.getFinalCalculation(validRequest)
          .returns(Future.successful(Left(Errors(error))))

        val result: CalculationOutcome = await(target.calculate(validRequest))
        result shouldBe Left(Errors(ApiServiceError))
      }
    }

    "an INVALID_CORRELATIONID error is returned" should {
      "convert the error to an InternalServerError" in new Test {
        val error = Error("INVALID_CORRELATIONID", "")
        MockedDesConnector.getFinalCalculation(validRequest)
          .returns(Future.successful(Left(Errors(error))))

        val result: CalculationOutcome = await(target.calculate(validRequest))
        result shouldBe Left(Errors(ApiServiceError))
      }
    }

    "an INVALID_NINO error is returned" should {
      "convert the error to an InternalServerError" in new Test {
        val error = Error("INVALID_NINO", "")
        MockedDesConnector.getFinalCalculation(validRequest)
          .returns(Future.successful(Left(Errors(error))))

        val result: CalculationOutcome = await(target.calculate(validRequest))
        result shouldBe Left(Errors(ApiServiceError))
      }
    }

    "an INVALID_PAYLOAD error is returned" should {
      "convert the error to an InternalServerError" in new Test {
        val error = Error("INVALID_PAYLOAD", "")
        MockedDesConnector.getFinalCalculation(validRequest)
          .returns(Future.successful(Left(Errors(error))))

        val result: CalculationOutcome = await(target.calculate(validRequest))
        result shouldBe Left(Errors(ApiServiceError))
      }
    }

    "a RETIREMENT_DATE_AFTER_DEATH error is returned" should {
      "convert the error to an RetirementAfterDeathError" in new Test {
        val error = Error("RETIREMENT_DATE_AFTER_DEATH", "")
        MockedDesConnector.getFinalCalculation(validRequest)
          .returns(Future.successful(Left(Errors(error))))

        val result: CalculationOutcome = await(target.calculate(validRequest))
        result shouldBe Left(Errors(RetirementAfterDeathError))
      }
    }

    "a TOO_EARLY error is returned" should {
      "convert the error to an TooEarlyError" in new Test {
        val error = Error("TOO_EARLY", "")
        MockedDesConnector.getFinalCalculation(validRequest)
          .returns(Future.successful(Left(Errors(error))))

        val result: CalculationOutcome = await(target.calculate(validRequest))
        result shouldBe Left(Errors(TooEarlyError))
      }
    }

    "a UNKNOWN_BUSINESS_ERROR error is returned" should {
      "convert the error to an UnknownBusinessError" in new Test {
        val error = Error("UNKNOWN_BUSINESS_ERROR", "")
        MockedDesConnector.getFinalCalculation(validRequest)
          .returns(Future.successful(Left(Errors(error))))

        val result: CalculationOutcome = await(target.calculate(validRequest))
        result shouldBe Left(Errors(UnknownBusinessError))
      }
    }

    "a NOT_FOUND_NINO error is returned" should {
      "convert the error to an NinoNotFoundError" in new Test {
        val error = Error("NOT_FOUND_NINO", "")
        MockedDesConnector.getFinalCalculation(validRequest)
          .returns(Future.successful(Left(Errors(error))))

        val result: CalculationOutcome = await(target.calculate(validRequest))
        result shouldBe Left(Errors(NinoNotFoundError))
      }
    }

    "a NO_MATCH_FOUND error is returned" should {
      "convert the error to an MatchNotFoundError" in new Test {
        val error = Error("NO_MATCH_FOUND", "")
        MockedDesConnector.getFinalCalculation(validRequest)
          .returns(Future.successful(Left(Errors(error))))

        val result: CalculationOutcome = await(target.calculate(validRequest))
        result shouldBe Left(Errors(MatchNotFoundError))
      }
    }

    "a SERVER_ERROR error is returned" should {
      "convert the error to an ApiServiceError" in new Test {
        val error = Error("SERVER_ERROR", "")
        MockedDesConnector.getFinalCalculation(validRequest)
          .returns(Future.successful(Left(Errors(error))))

        val result: CalculationOutcome = await(target.calculate(validRequest))
        result shouldBe Left(Errors(ApiServiceError))
      }
    }

    "a SERVICE_UNAVAILABLE error is returned" should {
      "convert the error to an ServiceUnavailableError" in new Test {
        val error = Error("SERVICE_UNAVAILABLE", "")
        MockedDesConnector.getFinalCalculation(validRequest)
          .returns(Future.successful(Left(Errors(error))))

        val result: CalculationOutcome = await(target.calculate(validRequest))
        result shouldBe Left(Errors(ServiceUnavailableError))
      }
    }

    "a MESSAGE_THROTTLED_OUT error is returned" should {
      "convert the error to an ThrottledError" in new Test {
        val error = Error("MESSAGE_THROTTLED_OUT", "")
        MockedDesConnector.getFinalCalculation(validRequest)
          .returns(Future.successful(Left(Errors(error))))

        val result: CalculationOutcome = await(target.calculate(validRequest))
        result shouldBe Left(Errors(ThrottledError))
      }
    }

  }
}

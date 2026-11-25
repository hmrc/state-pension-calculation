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

package services

import mocks.MockDesConnector
import models.errors.*
import models.{CalculationOutcome, CalculationRequest}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, verifyNoMoreInteractions}
import org.scalatest.TestSuite
import support.data.CalculationTestData.Response.expectedModel as validResponse
import utils.ErrorCodes.CalculationErrorCodePrefix

import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.{ExecutionContext, Future}

class CalculationServiceSpec extends ServiceBaseSpec {

  private trait Test extends TestSuite with MockDesConnector {
    lazy val target = new CalculationService(mockDesConnector)

    def verifyInitialCalculation(request: CalculationRequest): Unit = {
      verify(mockDesConnector)
        .getInitialCalculation(eqTo(request))(
          using any[HeaderCarrier],
          any[ExecutionContext]
        )
      verifyNoMoreInteractions(mockDesConnector)
    }

    def verifyFinalCalculation(request: CalculationRequest): Unit = {
      verify(mockDesConnector)
        .getFinalCalculation(eqTo(request))(
          using any[HeaderCarrier],
          any[ExecutionContext]
        )
      verifyNoMoreInteractions(mockDesConnector)
    }

  }

  "Calling calculate" when {

    val validRequest = CalculationRequest("AA12456A", "M", "SMIJ", finalCalculation = true)

    "the request is not final" should {

      "call the getInitialCalculation method on the connector" in new Test {
        val request = CalculationRequest("AA12456A", "M", "SMIJ")

        MockedDesConnector
          .getInitialCalculation(request)
          .thenReturn(Future.successful(Right(validResponse)))

        val result: CalculationOutcome = await(target.calculate(request))
        result shouldBe Right(validResponse)

        verifyInitialCalculation(request)
      }
    }

    "the request is final" should {
      "call the getFinalCalculation method on the connector" in new Test {
        val request = CalculationRequest("AA12456A", "M", "SMIJ", finalCalculation = true)

        MockedDesConnector
          .getFinalCalculation(request)
          .thenReturn(Future.successful(Right(validResponse)))

        val result: CalculationOutcome = await(target.calculate(request))
        result shouldBe Right(validResponse)

        verifyFinalCalculation(request)
      }
    }

    "an unknown error is returned" should {
      "convert the error to an InternalServerError" in new Test {
        val error = Error("UNKNOWN", "unknown message")
        MockedDesConnector
          .getFinalCalculation(validRequest)
          .thenReturn(Future.successful(Left(Errors(error))))

        val result: CalculationOutcome = await(target.calculate(validRequest))
        result shouldBe Left(Errors(ApiServiceError))

        verifyFinalCalculation(validRequest)
      }
    }

    "an INVALID_CORRELATIONID error is returned" should {
      "convert the error to an InternalServerError" in new Test {
        val error = Error("INVALID_CORRELATIONID", "")
        MockedDesConnector
          .getFinalCalculation(validRequest)
          .thenReturn(Future.successful(Left(Errors(error))))

        val result: CalculationOutcome = await(target.calculate(validRequest))
        result shouldBe Left(Errors(ApiServiceError))

        verifyFinalCalculation(validRequest)
      }
    }

    "an INVALID_NINO error is returned" should {
      "convert the error to an InternalServerError" in new Test {
        val error = Error("INVALID_NINO", "")
        MockedDesConnector
          .getFinalCalculation(validRequest)
          .thenReturn(Future.successful(Left(Errors(error))))

        val result: CalculationOutcome = await(target.calculate(validRequest))
        result shouldBe Left(Errors(ApiServiceError))

        verifyFinalCalculation(validRequest)
      }
    }

    "an INVALID_PAYLOAD error is returned" should {
      "convert the error to an InternalServerError" in new Test {
        val error = Error("INVALID_PAYLOAD", "")
        MockedDesConnector
          .getFinalCalculation(validRequest)
          .thenReturn(Future.successful(Left(Errors(error))))

        val result: CalculationOutcome = await(target.calculate(validRequest))
        result shouldBe Left(Errors(ApiServiceError))

        verifyFinalCalculation(validRequest)
      }
    }

    "a RETIREMENT_DATE_AFTER_DEATH_DATE error is returned" should {
      "convert the error to an RetirementAfterDeathError" in new Test {
        val error = Error("RETIREMENT_DATE_AFTER_DEATH_DATE", "")
        MockedDesConnector
          .getFinalCalculation(validRequest)
          .thenReturn(Future.successful(Left(Errors(error))))

        val result: CalculationOutcome = await(target.calculate(validRequest))
        result shouldBe Left(Errors(RetirementAfterDeathError))

        verifyFinalCalculation(validRequest)
      }
    }

    "a TOO_EARLY error is returned" should {
      "convert the error to an TooEarlyError" in new Test {
        val error = Error("TOO_EARLY", "")
        MockedDesConnector
          .getFinalCalculation(validRequest)
          .thenReturn(Future.successful(Left(Errors(error))))

        val result: CalculationOutcome = await(target.calculate(validRequest))
        result shouldBe Left(Errors(TooEarlyError))

        verifyFinalCalculation(validRequest)
      }
    }

    "a UNKNOWN_BUSINESS_ERROR error is returned" should {
      "convert the error to an UnknownBusinessError" in new Test {
        val error = Error("UNKNOWN_BUSINESS_ERROR", "")
        MockedDesConnector
          .getFinalCalculation(validRequest)
          .thenReturn(Future.successful(Left(Errors(error))))

        val result: CalculationOutcome = await(target.calculate(validRequest))
        result shouldBe Left(Errors(UnknownBusinessError))

        verifyFinalCalculation(validRequest)
      }
    }

    "a NOT_FOUND_NINO error is returned" should {
      "convert the error to an NinoNotFoundError" in new Test {
        val error = Error("NOT_FOUND_NINO", "")
        MockedDesConnector
          .getFinalCalculation(validRequest)
          .thenReturn(Future.successful(Left(Errors(error))))

        val result: CalculationOutcome = await(target.calculate(validRequest))
        result shouldBe Left(Errors(NinoNotFoundError))

        verifyFinalCalculation(validRequest)
      }
    }

    "a NO_MATCH_FOUND error is returned" should {
      "convert the error to an MatchNotFoundError" in new Test {
        val error = Error("NO_MATCH_FOUND", "")
        MockedDesConnector
          .getFinalCalculation(validRequest)
          .thenReturn(Future.successful(Left(Errors(error))))

        val result: CalculationOutcome = await(target.calculate(validRequest))
        result shouldBe Left(Errors(MatchNotFoundError))

        verifyFinalCalculation(validRequest)
      }
    }

    "a SERVER_ERROR error is returned" should {
      "convert the error to an ApiServiceError" in new Test {
        val error = Error("SERVER_ERROR", "")
        MockedDesConnector
          .getFinalCalculation(validRequest)
          .thenReturn(Future.successful(Left(Errors(error))))

        val result: CalculationOutcome = await(target.calculate(validRequest))
        result shouldBe Left(Errors(ApiServiceError))

        verifyFinalCalculation(validRequest)
      }
    }

    "a SERVICE_UNAVAILABLE error is returned" should {
      "convert the error to an ServiceUnavailableError" in new Test {
        val error = Error("SERVICE_UNAVAILABLE", "")
        MockedDesConnector
          .getFinalCalculation(validRequest)
          .thenReturn(Future.successful(Left(Errors(error))))

        val result: CalculationOutcome = await(target.calculate(validRequest))
        result shouldBe Left(Errors(ServiceUnavailableError))

        verifyFinalCalculation(validRequest)
      }
    }

    "a MESSAGE_THROTTLED_OUT error is returned" should {
      "convert the error to an ThrottledError" in new Test {
        val error = Error("MESSAGE_THROTTLED_OUT", "")
        MockedDesConnector
          .getFinalCalculation(validRequest)
          .thenReturn(Future.successful(Left(Errors(error))))

        val result: CalculationOutcome = await(target.calculate(validRequest))
        result shouldBe Left(Errors(ThrottledError))

        verifyFinalCalculation(validRequest)
      }
    }

    "a calculation error is returned" should {
      "prefix the error code with CALCULATION_ERROR_" in new Test {
        val calcError     = Error("12345", "Calc Error for scenario 12345")
        val expectedError = Error(CalculationErrorCodePrefix + "12345", "Calc Error for scenario 12345")
        MockedDesConnector
          .getFinalCalculation(validRequest)
          .thenReturn(Future.successful(Left(Errors(calcError))))

        val result: CalculationOutcome = await(target.calculate(validRequest))
        result shouldBe Left(Errors(expectedError))

        verifyFinalCalculation(validRequest)
      }
    }

    "multiple calculation errors are returned" should {
      "prefix each error code with CALCULATION_ERROR_" in new Test {
        val error1         = Error("12345", "Calc Error for scenario 12345")
        val error2         = Error("123456", "Calc Error for scenario 123456")
        val expectedError1 = Error(CalculationErrorCodePrefix + "12345", "Calc Error for scenario 12345")
        val expectedError2 = Error(CalculationErrorCodePrefix + "123456", "Calc Error for scenario 123456")
        MockedDesConnector
          .getFinalCalculation(validRequest)
          .thenReturn(Future.successful(Left(Errors(Seq(error1, error2)))))

        val result: CalculationOutcome = await(target.calculate(validRequest))
        result shouldBe Left(Errors(Seq(expectedError1, expectedError2)))

        verifyFinalCalculation(validRequest)
      }
    }

  }

}

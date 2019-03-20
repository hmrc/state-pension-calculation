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
import models.{CalculationOutcome, CalculationRequest}
import support.data.CalculationTestData.Response.{expectedModel => validResponse}

import scala.concurrent.Future

class CalculationServiceSpec extends ServiceBaseSpec {

  private trait Test extends MockDesConnector {
    lazy val target = new CalculationService(mockDesConnector)
  }

  "Calling calculate" when {

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

  }
}

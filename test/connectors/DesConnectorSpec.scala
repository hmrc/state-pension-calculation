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

package connectors

import mocks.{MockAppConfig, MockHttpClient}
import models.CalculationTestData.Response.{expectedModel => validResponse}
import models.errors.{InternalServerError, SingleError}
import models.{CalculationOutcome, CalculationRequest}
import play.api.http.HeaderNames

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DesConnectorSpec extends ConnectorBaseSpec {

  val baseUrl = "test-mtdIdBaseUrl"

  private trait Test extends MockHttpClient with MockAppConfig {

    val connector = new DesConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
  }

  "desHeaderCarrier" should {
    "return a header carrier with an authorization header using the DES token specified in config" in new Test {
      connector.desHeaderCarrier.headers.contains(HeaderNames.AUTHORIZATION -> "Bearer des-token") shouldBe true
    }

    "return a header carrier with an environment header using the DES environment specified in config" in new Test {
      connector.desHeaderCarrier.headers.contains("Environment" -> "des-environment") shouldBe true
    }
  }

  "getInitialCalculation" should {

    val nino = "AA12356A"
    val request = CalculationRequest(nino, "M", "SMIJ", None)
    val response = validResponse

    val url = s"$baseUrl/individuals/pensions/ltb-calculation/initial/$nino"

    "return a calculation" when {
      "the http client returns a success response" in new Test {
        MockedHttpClient.post(url, request)
          .returns(Future.successful(Right(response)))

        val result: CalculationOutcome = await(connector.getInitialCalculation(request))
        result shouldBe Right(response)
      }
    }

    "return an error" when {
      "the http client returns an error response" in new Test {
        val errorResponse = SingleError(InternalServerError)

        MockedHttpClient.post(url, request)
          .returns(Future.successful(Left(errorResponse)))

        val result: CalculationOutcome = await(connector.getInitialCalculation(request))
        result shouldBe Left(errorResponse)
      }
    }
  }

  "getFinalCalculation" should {

    val nino = "AA12356A"
    val request = CalculationRequest(nino, "M", "SMIJ", Some(BigDecimal("123.99")))
    val response = validResponse

    val url = s"$baseUrl/individuals/pensions/ltb-calculation/final/$nino"

    "return a calculation" when {
      "the http client returns a success response" in new Test {
        MockedHttpClient.post(url, request)
          .returns(Future.successful(Right(response)))

        val result: CalculationOutcome = await(connector.getFinalCalculation(request))
        result shouldBe Right(response)
      }
    }

    "return an error" when {
      "the http client returns an error response" in new Test {
        val errorResponse = SingleError(InternalServerError)

        MockedHttpClient.post(url, request)
          .returns(Future.successful(Left(errorResponse)))

        val result: CalculationOutcome = await(connector.getFinalCalculation(request))
        result shouldBe Left(errorResponse)
      }
    }
  }

}

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
import play.api.http.Status
import uk.gov.hmrc.api.domain.Registration
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, TimeoutException}

class ServiceLocatorRegistrationConnectorSpec extends ConnectorBaseSpec {

  private trait Test extends MockAppConfig with MockHttpClient {

    MockedAppConfig.serviceLocatorBaseUrl().returns("http://service-locator")
    MockedAppConfig.apiServiceName().returns("service-name")
    MockedAppConfig.discoveryUrl().returns("self-url")

    val body = Registration("service-name", "self-url", Some(Map("third-party-api" -> "true")))

    val serviceResponse: Future[Any]

    lazy val target: ServiceLocatorRegistrationConnector = {
      MockedHttpClient.post("http://service-locator/registration", body, Seq("Content-Type" -> "application/json"))
        .returns(serviceResponse)
      new ServiceLocatorRegistrationConnector(mockAppConfig, mockHttpClient)
    }
  }

  "Calling register" when {

    "the POST is successful" should {
      "return true" in new Test {
        override val serviceResponse: Future[Any] = Future.successful(HttpResponse(Status.NO_CONTENT))
        await(target.register()) shouldBe true
      }
    }

    "the POST fails" should {
      "return false" in new Test {
        override val serviceResponse: Future[Any] = Future.failed(new TimeoutException())
        await(target.register()) shouldBe false
      }
    }
  }

}

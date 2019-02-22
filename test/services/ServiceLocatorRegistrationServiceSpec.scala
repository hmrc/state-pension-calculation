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

import mocks.{MockAppConfig, MockServiceLocatorRegistrationConnector}

import scala.concurrent.Future

class ServiceLocatorRegistrationServiceSpec extends ServiceBaseSpec {

  private trait Test extends MockAppConfig with MockServiceLocatorRegistrationConnector {
    def executeTest(): Unit = new ServiceLocatorRegistrationService(mockAppConfig, mockServiceLocatorRegistrationConnector)
  }

  "Calling register" when {

    "registration has been enabled and registration is successful" should {
      "call the registration microservice once" in new Test {
        MockedAppConfig.registrationEnabled().returns(true)
        MockedServiceLocatorRegistrationConnector.register()
          .returns(Future.successful(true))
          .once()
        executeTest()
      }
    }

    "registration has been enabled and registration fails" should {
      "call the registration microservice once" in new Test {
        MockedAppConfig.registrationEnabled().returns(true)
        MockedServiceLocatorRegistrationConnector.register()
          .returns(Future.successful(false))
          .once()
        executeTest()
      }
    }

    "registration has been disabled" should {
      "not call the registration microservice" in new Test {
        MockedAppConfig.registrationEnabled().returns(false)
        MockedServiceLocatorRegistrationConnector.register()
          .never()
        executeTest()
      }
    }
  }
}

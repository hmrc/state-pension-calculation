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

package config

import org.mockito.Mockito.{mock, verify, when}
import support.UnitSpec
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class AppConfigSpec extends UnitSpec {

  trait Test {
    lazy val mockConfig: ServicesConfig = mock(classOf[ServicesConfig])

    lazy val target: AppConfigImpl =
      new AppConfigImpl(mockConfig)

  }

  "calling desBaseUrl" should {
    "retrieve the DES URL" in new Test {

      val url = "http://des-host"

      when(mockConfig.baseUrl("des"))
        .thenReturn(url)

      target.desBaseUrl() shouldBe url

      verify(mockConfig).baseUrl("des")
    }
  }

  "calling desEnvironment" when {

    "a value is added to the configuration" should {
      "return the business details env" in new Test {

        val env = "TEST_ENV"

        when(mockConfig.getString("microservice.services.des.env"))
          .thenReturn(env)

        target.desEnvironment() shouldBe env
      }
    }

    "no value added to the configuration" should {
      "return the run time exception" in new Test {
        when(mockConfig.getString("microservice.services.des.env"))
          .thenThrow(new RuntimeException("Missing DES environment"))

        intercept[RuntimeException] {
          target.desEnvironment()
        }
      }
    }
  }

  "calling desToken" when {

    "token is added to the configuration" should {
      "return the DES token" in new Test {

        val token = "some-token"

        when(mockConfig.getString("microservice.services.des.token"))
          .thenReturn(token)

        target.desToken() shouldBe token
      }
    }

    "no value added to the configuration" should {
      "return the run time exception" in new Test {
        when(mockConfig.getString("microservice.services.des.token"))
          .thenThrow(new RuntimeException("Missing DES token"))

        intercept[RuntimeException] {
          target.desToken()
        }
      }
    }
  }

}

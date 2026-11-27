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

import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{mock, verify, when}
import play.api.{ConfigLoader, Configuration}
import support.UnitSpec

class ApiDefinitionConfigSpec extends UnitSpec {

  private trait Test {
    lazy val mockConfig: Configuration = mock(classOf[Configuration])

    lazy val target: ApiDefinitionConfigImpl =
      new ApiDefinitionConfigImpl(mockConfig)

  }

  "calling status" when {

    "a value is added to the configuration" should {
      "retrieve the status specified" in new Test {

        val expected = "BETA"

        when(
          mockConfig.get[String](eqTo("api.status"))(
            using any[ConfigLoader[String]]
          )
        ).thenReturn(expected)

        target.status() shouldBe expected

        verify(mockConfig).get[String](eqTo("api.status"))(
          using any[ConfigLoader[String]]
        )
      }
    }

    "no value is added to the configuration" should {
      "return a runtime exception" in new Test {
        when(
          mockConfig.get[String](eqTo("api.status"))(
            using any[ConfigLoader[String]]
          )
        ).thenThrow(new RuntimeException("Missing api.status"))

        intercept[RuntimeException] {
          target.status()
        }
      }
    }
  }

  "calling accessType" when {

    "a value is added to the configuration" should {
      "retrieve the API access setting specified" in new Test {

        val expected = "PUBLIC"

        when(
          mockConfig.getOptional[String](eqTo("api.access"))(
            using any[ConfigLoader[String]]
          )
        ).thenReturn(Some(expected))

        target.accessType() shouldBe expected

        verify(mockConfig).getOptional[String](eqTo("api.access"))(
          using any[ConfigLoader[String]]
        )
      }
    }

    "no value is added to the configuration" should {
      "retrieve the default API access setting (INTERNAL)" in new Test {

        val expected = "INTERNAL"

        when(
          mockConfig.getOptional[String](eqTo("api.access"))(
            using any[ConfigLoader[String]]
          )
        ).thenReturn(None)

        target.accessType() shouldBe expected

        verify(mockConfig).getOptional[String](eqTo("api.access"))(
          using any[ConfigLoader[String]]
        )
      }
    }
  }

}

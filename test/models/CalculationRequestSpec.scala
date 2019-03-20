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

package models

import play.api.libs.json.Json
import support.UnitSpec

class CalculationRequestSpec extends UnitSpec {

  "Generating JSON from a CalculationRequest with a fry amount" should {

    val request = CalculationRequest("some nino",
      "a gender",
      "a check brick",
      finalCalculation = true,
      Some(BigDecimal("123.99"))
    )

    val json = Json.toJson(request)

    "create a correctly formatted JSON object including the fry amount" in {
      val expectedJson = Json.parse(
        """
          |{
          |  "gender": "a gender",
          |  "checkbrick": "a check brick",
          |  "totalPrimaryEarningsForFry": 123.99
          |}
        """.stripMargin
      )

      json shouldBe expectedJson
    }
  }

  "Generating JSON from a CalculationRequest without a fry amount" should {

    val request = CalculationRequest("some nino",
      "a gender",
      "a check brick"
    )

    val json = Json.toJson(request)

    "create a correctly formatted JSON object without the fry amount" in {
      val expectedJson = Json.parse(
        """
          |{
          |  "gender": "a gender",
          |  "checkbrick": "a check brick"
          |}
        """.stripMargin
      )

      json shouldBe expectedJson
    }
  }
}

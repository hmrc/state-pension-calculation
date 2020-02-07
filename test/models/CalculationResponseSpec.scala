/*
 * Copyright 2020 HM Revenue & Customs
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

class CalculationResponseSpec extends UnitSpec {

  "Parsing valid JSON that represents a CalculationResponse with notes" when {
    import support.data.CalculationTestData.Response._

    "from the initial calc API" should {
      "generate a valid CalculationResponse" in {
        val result = initialCalcJson.as[CalculationResponse]
        result shouldBe expectedModel
      }
    }

    "from the final calc API" should {
      "generate a valid CalculationResponse" in {
        val result = finalCalcJson.as[CalculationResponse]
        result shouldBe expectedModel
      }
    }
  }

  "Parsing valid JSON that represents a CalculationResponse without notes" when {
    import support.data.CalculationTestData.ResponseWithoutNotes._

    "from the initial calc API" should {
      "generate a valid CalculationResponse" in {
        val result = initialCalcJson.as[CalculationResponse]
        result shouldBe expectedModel
      }
    }

    "from the final calc API" should {
      "generate a valid CalculationResponse" in {
        val result = finalCalcJson.as[CalculationResponse]
        result shouldBe expectedModel
      }
    }
  }

  "Serialising a CalculationResponse with notes to JSON" should {
    "generate JSON that conforms to the JSON schema for a calc response" in {
      import support.data.CalculationTestData.Response._
      val result = Json.toJson(expectedModel)
      val expected = generatedJson
      result shouldBe expected
    }
  }

  "Serialising a CalculationResponse without notes to JSON" should {
    "generate JSON that conforms to the JSON schema for a calc response" in {
      import support.data.CalculationTestData.ResponseWithoutNotes._
      val result = Json.toJson(expectedModel)
      val expected = generatedJson
      result shouldBe expected
    }
  }
}
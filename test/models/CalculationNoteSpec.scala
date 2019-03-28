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

import support.data.CalculationTestData.Notes._
import play.api.libs.json.Json
import support.UnitSpec

class CalculationNoteSpec extends UnitSpec {

  "Parsing valid JSON that represents a note" should {
    "generate a valid CalculationNote" in {
      val note = Json.parse(json).as[CalculationNote]
      note shouldBe expectedModel
    }
  }

  "Parsing valid JSON that represents a note without any lines" should {
    "generate a valid CalculationNote" in {
      val note = Json.parse(emptyJson).as[CalculationNote]
      note shouldBe expectedEmptyModel
    }
  }

  "Serialising CalculationNotes to JSON" should {
    "generate JSON that conforms to the JSON schema for calc notes" in {
      val result = Json.toJson(expectedModel)
      val expected = Json.parse(generatedJson)
      result shouldBe expected
    }
  }
}

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

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class CalculationResponse(result: CalculationResult,
                               notes: Seq[CalculationNote],
                               qualifyingYears: Seq[QualifyingYear])

object CalculationResponse {
  implicit val writes: OWrites[CalculationResponse] = Json.writes[CalculationResponse]

  implicit val reads: Reads[CalculationResponse] = (
    (__ \ "initialRequestResult").read[CalculationResult] or
      (__ \ "finalRequestResult").read[CalculationResult] and
      (__ \ "associatedNotes").read[Seq[CalculationNote]] and
      (__ \ "listOfQualifyingYears").read[Seq[QualifyingYear]]
  )(CalculationResponse.apply _)

}

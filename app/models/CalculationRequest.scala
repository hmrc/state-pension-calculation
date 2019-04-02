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

import scala.util.matching.Regex

case class CalculationRequest(nino: String,
                              gender: String,
                              checkBrick: String,
                              finalCalculation: Boolean = false,
                              fryAmount: Option[BigDecimal] = None)

object CalculationRequest {
  val ninoPattern: Regex = """^((?!(BG|GB|KN|NK|NT|TN|ZZ)|(D|F|I|Q|U|V)[A-Z]|[A-Z](D|F|I|O|Q|U|V))[A-Z]{2})[0-9]{6}[A-D\s]?$""".r
  val checkBrickPattern: Regex = """^([A-Z]{1}[ A-Z-']{2,4})$""".r
  val genderPattern: Regex = """^[MF]$""".r

  implicit val reads: Reads[CalculationRequest] =(
    (__ \ "nino").read[String](Reads.pattern(ninoPattern)) and
    (__ \ "gender").read[String](Reads.pattern(genderPattern)) and
    (__ \ "checkBrick").read[String](Reads.pattern(checkBrickPattern)) and
    (__ \ "finalise").read[Boolean] and
    (__ \ "fryAmount").readNullable[BigDecimal]
  ) (CalculationRequest.apply _)

  implicit val writes: Writes[CalculationRequest] = new Writes[CalculationRequest] {
    override def writes(request: CalculationRequest): JsValue = {
      val json = Json.obj(
        "gender" -> request.gender,
        "checkbrick" -> request.checkBrick
      )

      request.fryAmount .fold(json) { amount =>
        json + ("totalPrimaryEarningsForFry" -> Json.toJson(amount))
      }
    }
  }
}

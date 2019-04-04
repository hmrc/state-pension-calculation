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

package models.errors

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Error(code: String, message: String)

object Error {
  implicit val writes: Writes[Error] = Json.writes[Error]
  implicit val reads: Reads[Error] = (
    (__ \ "code").read[String] and
      (__ \ "reason").read[String]
    ) (Error.apply _)
}

object ApiServiceError
  extends Error("INTERNAL_SERVER_ERROR", "An error occurred whilst processing your request.")

object InvalidRequestError
  extends Error("INVALID_REQUEST", "The request is invalid.")

object UnexpectedFryAmountError
  extends Error("FRY_AMOUNT_NOT_EXPECTED", "Do not supply a FRY amount for an initial calculation.")

object RetirementAfterDeathError
  extends Error("RETIREMENT_DATE_AFTER_DEATH", "The remote endpoint has indicated that the Date of Retirement is after the Date of Death.")

object TooEarlyError
  extends Error("TOO_EARLY", "The remote endpoint has indicated that the pension calculation can only be done within 6 months of the SPA date.")

object UnknownBusinessError
  extends Error("UNKNOWN_BUSINESS_ERROR", "The remote endpoint has returned an unknown business validation error.")

object NinoNotFoundError
  extends Error("NOT_FOUND_NINO", "The remote endpoint has indicated that the Nino provided cannot be found.")

object MatchNotFoundError
  extends Error("NOT_FOUND_MATCH", "The remote endpoint has indicated that there is no match for the person details provided.")

object ServiceUnavailableError
  extends Error("SERVER_ERROR", "Service unavailable.")

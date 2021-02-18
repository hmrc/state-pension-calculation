/*
 * Copyright 2021 HM Revenue & Customs
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

package utils

object ErrorCodes {
  val InvalidCorrelationIdCode: String = "INVALID_CORRELATIONID"
  val InvalidNinoCode: String = "INVALID_NINO"
  val InvalidPayloadCode: String = "INVALID_PAYLOAD"
  val RetirementDateAfterDeathDateCode: String = "RETIREMENT_DATE_AFTER_DEATH_DATE"
  val TooEarlyCode: String = "TOO_EARLY"
  val UnknownBusinessErrorCode: String = "UNKNOWN_BUSINESS_ERROR"
  val NinoNotFoundCode: String = "NOT_FOUND_NINO"
  val MatchNotFoundCode: String = "NO_MATCH_FOUND"
  val ServerErrorCode: String = "SERVER_ERROR"
  val ServiceUnavailableCode: String = "SERVICE_UNAVAILABLE"
  val MessageThrottledCode: String = "MESSAGE_THROTTLED_OUT"
  val InternalServerErrorCode: String = "INTERNAL_SERVER_ERROR"
  val CalculationErrorCodePrefix: String = "CALCULATION_ERROR_"
}

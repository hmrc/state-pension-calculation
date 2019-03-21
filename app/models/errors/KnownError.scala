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

case class KnownError(code: String, message: String)

object KnownError {
  implicit val writes: Writes[KnownError] = Json.writes[KnownError]
  implicit val reads: Reads[KnownError] = (
    (__ \ "code").read[String] and
      (__ \ "reason").read[String]
    ) (KnownError.apply _)
}

object InternalServerError
  extends KnownError("INTERNAL_SERVER_ERROR", "Internal server error.")

object InvalidRequestError
  extends KnownError("INVALID_REQUEST", "The request is invalid.")

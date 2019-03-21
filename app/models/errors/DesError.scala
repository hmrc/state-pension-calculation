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

import play.api.libs.json.{JsValue, Json, Writes}

sealed trait DesError

case class SingleError(error: KnownError) extends DesError

object SingleError {
  implicit val writes: Writes[SingleError] = new Writes[SingleError] {
    override def writes(data: SingleError): JsValue = Json.toJson(data.error)
  }
}

case class MultipleErrors(errors: Seq[KnownError]) extends DesError

object MultipleErrors {
  implicit val writes: Writes[MultipleErrors] = new Writes[MultipleErrors] {
    override def writes(data: MultipleErrors): JsValue = Json.obj(
      "errors" -> Json.toJson(data.errors)
    )
  }
}
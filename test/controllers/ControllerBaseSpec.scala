/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.http.{HeaderNames, MimeTypes, Status}
import play.api.mvc.{AnyContentAsEmpty, ControllerComponents}
import play.api.test.Helpers.stubControllerComponents
import play.api.test.{FakeRequest, ResultExtractors}
import support.UnitSpec
import utils.AdditionalHeaderNames.CorrelationIdHeader


class ControllerBaseSpec extends UnitSpec
  with Status
  with MimeTypes
  with HeaderNames
  with ResultExtractors {

  val correlationId: String = UUID.randomUUID().toString

  implicit val system: ActorSystem = ActorSystem("MyTest")
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit lazy val validRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest().withHeaders(CorrelationIdHeader -> correlationId)

  lazy val cc: ControllerComponents = stubControllerComponents()

  def postRequest[T](body: T): FakeRequest[T] = validRequest.withBody(body)
}

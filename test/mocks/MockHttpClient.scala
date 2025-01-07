/*
 * Copyright 2024 HM Revenue & Customs
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

package mocks

import models.CalculationOutcome
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.http
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.http.client.HttpClientV2

import scala.concurrent.{ExecutionContext, Future}

trait MockHttpClient extends MockFactory {

  val mockHttpClient: HttpClientV2 = mock[HttpClientV2]

  object MockedHttpClient {

    def post[I, O](url: String, body: I, headers: Seq[(String, String)]): CallHandler[Future[O]] = {
     (mockHttpClient.POST[I, O](_: String, _: I, _: Seq[(String, String)])
        (_: Writes[I], _: HttpReads[O], _: HeaderCarrier, _: ExecutionContext))
       .expects(url, body, headers, *, *, *, *)
    }


   def post[I, O](url: String, body: I): CallHandler[Future[O]] = {
      (mockHttpClient.POST[I, O](_: String, _: I, _: Seq[(String, String)])
       (_: Writes[I], _: HttpReads[O], _: HeaderCarrier, _: ExecutionContext))
        .expects(url, body, *, *, *, *, *)
   }

  }

}

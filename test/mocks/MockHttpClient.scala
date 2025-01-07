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

import izumi.reflect.Tag
import org.scalamock.scalatest.MockFactory
import play.api.libs.json._
import play.api.libs.ws.BodyWritable
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, StringContextOps}

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}

trait MockHttpClient extends MockFactory {

  val mockHttpClient: HttpClientV2 = mock[HttpClientV2]
  val mockRequestBuilder: RequestBuilder = mock[RequestBuilder]

  object MockedHttpClient {

    def post[O](url: String, body: JsValue)(response: Future[O]): Unit = {
      (mockHttpClient
        .post(_: URL)(_: HeaderCarrier))
        .expects(url"$url", *)
        .returns(mockRequestBuilder)

      (mockRequestBuilder
        .withBody(_: JsValue)(_: BodyWritable[JsValue], _: Tag[JsValue], _: ExecutionContext))
        .expects(body, *, *, *)
        .returns(mockRequestBuilder)

      (mockRequestBuilder
        .setHeader(_: (String, String)))
        .expects(*)
        .returns(mockRequestBuilder)

      (mockRequestBuilder
        .execute(_: HttpReads[O], _: ExecutionContext))
        .expects(*, *)
        .returns(response)
    }
  }

}

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
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{mock, when}
import org.scalatest.TestSuite
import play.api.libs.json.*
import play.api.libs.ws.BodyWritable
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, StringContextOps}

import scala.concurrent.{ExecutionContext, Future}

trait MockHttpClient {
  this: TestSuite =>

  val mockHttpClient: HttpClientV2       = mock(classOf[HttpClientV2])
  val mockRequestBuilder: RequestBuilder = mock(classOf[RequestBuilder])

  object MockedHttpClient {

    def post[O](url: String, body: JsValue)(response: Future[O]): Unit = {
      when(
        mockHttpClient.post(eqTo(url"$url"))(any[HeaderCarrier])
      ).thenReturn(mockRequestBuilder)

      when(
        mockRequestBuilder.withBody(eqTo(body))(
          any[BodyWritable[JsValue]],
          any[Tag[JsValue]],
          any[ExecutionContext]
        )
      ).thenReturn(mockRequestBuilder)

      when(
        mockRequestBuilder.setHeader(any[(String, String)])
      ).thenReturn(mockRequestBuilder)

      when(
        mockRequestBuilder.execute[O](
          any[HttpReads[O]],
          any[ExecutionContext]
        )
      ).thenReturn(response)
    }

  }

}

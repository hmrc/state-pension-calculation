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

package connectors.httpParsers

import models.errors.{Errors, ApiServiceError}
import models.{CalculationOutcome, CalculationResponse}
import play.api.Logger
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object GetCalculationHttpParser extends HttpParser {

  implicit val getCalculationHttpReads: HttpReads[CalculationOutcome] =
    new HttpReads[CalculationOutcome] {
      override def read(method: String, url: String, response: HttpResponse): CalculationOutcome = {

        if (response.status != CREATED) {
          val correlationId = response.header("CorrelationId").getOrElse("NOT FOUND")

          Logger.warn("[GetCalculationHttpParser][read] - " +
            s"Error response received from DES when calling $url\n" +
            s"status code: ${response.status}\n" +
            s"correlation ID: $correlationId\n" +
            s"body: ${response.body}")
        }

        response.status match {

          case CREATED => response.validateJson[CalculationResponse] match {
            case Some(result) => Right(result)
            case None => Left(Errors(ApiServiceError))
          }
          case _ => Left(parseErrors(response))
        }
      }
    }
}

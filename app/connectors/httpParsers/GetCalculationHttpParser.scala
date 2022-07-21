/*
 * Copyright 2022 HM Revenue & Customs
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

import models.errors.{ApiServiceError, Error, Errors, ThrottledError}
import models.{CalculationOutcome, CalculationResponse}
import play.api.Logging
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object GetCalculationHttpParser extends HttpParser with Logging {

  implicit val getCalculationHttpReads: HttpReads[CalculationOutcome] =
    new HttpReads[CalculationOutcome] {
      override def read(method: String, url: String, response: HttpResponse): CalculationOutcome = {

        if (response.status != CREATED) {
          val correlationId = response.header("CorrelationId").getOrElse("NOT FOUND")

          logger.warn("[GetCalculationHttpParser][read] - Error response received from DES\n" +
            s"URL: $url\n" +
            s"Status code: ${response.status}\n" +
            s"Correlation ID: $correlationId\n" +
            s"Body: ${response.body}"
          )
        }

        response.status match {

          case CREATED => response.validateJson[CalculationResponse] match {
            case Some(result) => Right(result)
            case None => Left(Errors(ApiServiceError))
          }
          case TOO_MANY_REQUESTS => Left(Errors(ThrottledError))
          case SERVICE_UNAVAILABLE => Left(parseServiceUnavailableError(response))
          case BAD_GATEWAY => Left(Errors(Seq(Error(BAD_GATEWAY.toString, response.body))))
          case _ => Left(parseErrors(response))
        }
      }
    }
}

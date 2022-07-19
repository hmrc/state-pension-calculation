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

package connectors

import config.AppConfig
import models.{CalculationOutcome, CalculationRequest}
import play.api.http.HeaderNames
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.AdditionalHeaderNames.{CorrelationIdHeader, Environment}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
@Singleton
class DesConnector @Inject()(http: HttpClient,
                             appConfig: AppConfig) {
  private[connectors] def desHeaders(correlationId: String): Seq[(String, String)] =
    Seq(
      "Authorization" -> s"Bearer ${appConfig.desToken()}",
      Environment -> appConfig.desEnvironment(),
      CorrelationIdHeader -> correlationId,
      HeaderNames.CONTENT_TYPE -> "application/json"
    )
  def getInitialCalculation(request: CalculationRequest)
                           (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CalculationOutcome] = {
    import connectors.httpParsers.GetCalculationHttpParser.getCalculationHttpReads
    val url = s"${appConfig.desBaseUrl()}/individuals/pensions/ltb-calculation/initial/${request.nino}"
    http.POST(url, request, desHeaders(request.correlationId))(implicitly, getCalculationHttpReads, hc, implicitly)
  }
  def getFinalCalculation(request: CalculationRequest)
                         (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CalculationOutcome] = {
    import connectors.httpParsers.GetCalculationHttpParser.getCalculationHttpReads
    val url = s"${appConfig.desBaseUrl()}/individuals/pensions/ltb-calculation/final/${request.nino}"
    http.POST(url, request, desHeaders(request.correlationId))(implicitly, getCalculationHttpReads, hc, implicitly)
  }
}

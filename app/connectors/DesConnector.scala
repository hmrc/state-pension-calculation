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

package connectors

import config.AppConfig
import javax.inject.{Inject, Singleton}
import models.{CalculationOutcome, CalculationRequest}
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.AdditionalHeaderNames.CorrelationIdHeader

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DesConnector @Inject()(http: HttpClient,
                             appConfig: AppConfig) {

  private[connectors] def desHeaderCarrier(correlationId: String)(implicit hc: HeaderCarrier): HeaderCarrier = hc
    .copy(authorization = Some(Authorization(s"Bearer ${appConfig.desToken()}")))
    .withExtraHeaders(
      "Environment" -> appConfig.desEnvironment(),
      CorrelationIdHeader -> correlationId
    )

  def getInitialCalculation(request: CalculationRequest)
                           (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CalculationOutcome] = {
    import connectors.httpParsers.GetCalculationHttpParser.getCalculationHttpReads

    val url = s"${appConfig.desBaseUrl()}/individuals/pensions/ltb-calculation/initial/${request.nino}"

    http.POST(url, request)(implicitly, getCalculationHttpReads, desHeaderCarrier(request.correlationId), implicitly)
  }

  def getFinalCalculation(request: CalculationRequest)
                         (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CalculationOutcome] = {
    import connectors.httpParsers.GetCalculationHttpParser.getCalculationHttpReads

    val url = s"${appConfig.desBaseUrl()}/individuals/pensions/ltb-calculation/final/${request.nino}"

    http.POST(url, request)(implicitly, getCalculationHttpReads, desHeaderCarrier(request.correlationId), implicitly)
  }
}

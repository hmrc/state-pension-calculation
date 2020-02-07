/*
 * Copyright 2020 HM Revenue & Customs
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

package services

import connectors.DesConnector
import javax.inject.{Inject, Singleton}
import models.errors._
import models.{CalculationOutcome, CalculationRequest}
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import utils.ErrorCodes._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CalculationService @Inject()(connector: DesConnector) {

  def calculate(request: CalculationRequest)
               (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CalculationOutcome] = {

    val unexpectedErrorMapping: String => Error = code => {
      Logger.warn(s"[CalculationService][calculate] Unexpected error received from DES. Code: $code")
      ApiServiceError
    }

    val errorMappings: Map[String, Error] = Map(
      InvalidCorrelationIdCode -> ApiServiceError,
      InvalidNinoCode -> ApiServiceError,
      InvalidPayloadCode -> ApiServiceError,
      RetirementDateAfterDeathDateCode -> RetirementAfterDeathError,
      TooEarlyCode -> TooEarlyError,
      UnknownBusinessErrorCode -> UnknownBusinessError,
      NinoNotFoundCode -> NinoNotFoundError,
      MatchNotFoundCode -> MatchNotFoundError,
      ServerErrorCode -> ApiServiceError,
      ServiceUnavailableCode -> ServiceUnavailableError,
      MessageThrottledCode -> ThrottledError,
      InternalServerErrorCode -> ApiServiceError
    ).withDefault(unexpectedErrorMapping)

    val result = if (request.finalCalculation) {
      connector.getFinalCalculation(request)
    } else {
      connector.getInitialCalculation(request)
    }

    result.map {
      case calculation@Right(_) => calculation
      case Left(Errors(errors)) =>
        val calculationErrorCodePattern = """^[0-9]{5,6}$"""
        val apiErrors = if (errors.forall(_.code.matches(calculationErrorCodePattern))) {
          errors.map { error => Error(CalculationErrorCodePrefix + error.code, error.message) }
        } else {
          val desErrorCodes = errors.map(_.code)
          desErrorCodes.map(errorMappings).distinct
        }
        Left(Errors(apiErrors))
    }
  }

}

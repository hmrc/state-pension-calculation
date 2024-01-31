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

package controllers

import javax.inject.{Inject, Singleton}
import models.CalculationRequest
import models.errors._
import play.api.libs.json.{JsSuccess, JsValue, Json}
import play.api.mvc._
import services.CalculationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.AdditionalHeaderNames.CorrelationIdHeader
import utils.ErrorCodes.CalculationErrorCodePrefix

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton()
class CalculationController @Inject()(cc: ControllerComponents, service: CalculationService)
  extends BackendController(cc) {

  private def handleErrors(errors: Errors): Result = {

    val handleNonStandardError: Error => Int = error => {
      if (error.code.startsWith(CalculationErrorCodePrefix)) {
        FORBIDDEN
      } else {
        INTERNAL_SERVER_ERROR
      }
    }

    val errorToResponseMapping: Map[Error, Int] = Map(
      RetirementAfterDeathError -> FORBIDDEN,
      TooEarlyError -> FORBIDDEN,
      UnknownBusinessError -> FORBIDDEN,
      NinoNotFoundError -> NOT_FOUND,
      MatchNotFoundError -> NOT_FOUND,
      ServiceUnavailableError -> SERVICE_UNAVAILABLE,
      ThrottledError -> TOO_MANY_REQUESTS
    ).withDefault(handleNonStandardError)

    val statusCode: Int = errorToResponseMapping(errors.errors.head)

    if (statusCode == INTERNAL_SERVER_ERROR) {
      Status(statusCode)(Json.toJson[Error](ApiServiceError))
    } else {
      Status(statusCode)(Json.toJson(errors))
    }
  }

  private def calculate(calculationRequest: CalculationRequest)(implicit hc: HeaderCarrier): Future[Result] = {
    service.calculate(calculationRequest).map {
      case Right(result) => Created(Json.toJson(result))
      case Left(errors) => handleErrors(errors)
    }
  }

  private def buildRequest(request: Request[JsValue]): Either[Errors, CalculationRequest] = {
    val correlationIdPattern = "^[A-Za-z0-9\\-]{36}$"
    request.headers.get(CorrelationIdHeader).map { correlationId =>
      request.body.validate[CalculationRequest] match {
        case JsSuccess(CalculationRequest(_, _, _, false, Some(_), _), _) =>
          Left(Errors(UnexpectedFryAmountError))
        case JsSuccess(calculationRequest, _) if correlationId.matches(correlationIdPattern) =>
          Right(calculationRequest.copy(correlationId = correlationId))
        case _ =>
          Left(Errors(InvalidRequestError))
      }
    }.getOrElse(Left(Errors(InvalidRequestError)))
  }

  def calculation(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val response = buildRequest(request) match {
      case Right(calcRequest) => calculate(calcRequest)
      case Left(error) => Future.successful(BadRequest(Json.toJson(error)))
    }

    response map { result =>
      val correlationId = request.headers.get(CorrelationIdHeader).getOrElse("")
      result.withHeaders(CorrelationIdHeader -> correlationId)
    }
  }

}

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

import config.ApiDefinitionConfig
import javax.inject.{Inject, Singleton}
import play.api.http.HttpErrorHandler
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.api.controllers.DocumentationController

import scala.concurrent.Future

@Singleton()
class ApiDocumentationController @Inject() (
    cc: ControllerComponents,
    assets: Assets,
    errorHandler: HttpErrorHandler,
    apiConfig: ApiDefinitionConfig
) extends DocumentationController(cc, assets, errorHandler) {

  override def definition(): Action[AnyContent] = Action.async {

    lazy val apiAccess: JsObject = Json.obj(
      "type" -> apiConfig.accessType()
    )

    val apiDefinition = Json.parse(
      s"""
         |{
         |  "api": {
         |    "name": "Get State Pension Calculation",
         |    "description": "Get an Individuals State Pension calculation",
         |    "context": "individuals/state-pension-calculation",
         |    "categories": ["PRIVATE_GOVERNMENT"],
         |    "versions": [
         |      {
         |        "version": "1.0",
         |        "status": "${apiConfig.status()}",
         |        "endpointsEnabled": ${apiConfig.endpointsEnabled()},
         |        "access" : $apiAccess
         |      }
         |    ]
         |  }
         |}
      """.stripMargin
    )

    Future.successful(Ok(apiDefinition))
  }

}

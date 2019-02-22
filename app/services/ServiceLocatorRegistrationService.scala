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

package services

import config.AppConfig
import connectors.ServiceLocatorRegistrationConnector
import javax.inject.{Inject, Singleton}
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class ServiceLocatorRegistrationService @Inject()(configuration: AppConfig,
                                                  connector: ServiceLocatorRegistrationConnector) {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  if (configuration.registrationEnabled()) {
    register()
  } else {
    Logger.info("Registration with service locator has been disabled")
  }

  def register(): Unit = connector.register().map {
    case true => Logger.info("Registration with service locator was successful")
    case false => Logger.info("Registration with service locator failed")
  }

}

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

package config

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

trait AppConfig {

  def serviceLocatorBaseUrl(): String

  def apiServiceName(): String

  def registrationEnabled(): Boolean

  def discoveryUrl(): String

  def desBaseUrl(): String

  def desEnvironment(): String

  def desToken(): String
}

@Singleton
class AppConfigImpl @Inject()(configuration: ServicesConfig)
  extends AppConfig {

  private val servicesPrefix = "microservice.services"
  private val serviceLocatorServicePrefix = s"$servicesPrefix.service-locator"
  private val desServicePrefix = s"$servicesPrefix.des"

  override lazy val serviceLocatorBaseUrl: String = configuration.baseUrl("service-locator")
  override lazy val apiServiceName: String = configuration.getString(s"$serviceLocatorServicePrefix.service-name")
  override lazy val registrationEnabled: Boolean = configuration.getBoolean(s"$serviceLocatorServicePrefix.enabled")
  override lazy val discoveryUrl: String = configuration.getString(s"$serviceLocatorServicePrefix.discovery-url")
  override lazy val desBaseUrl: String = configuration.baseUrl("des")
  override lazy val desEnvironment: String = configuration.getString(s"$desServicePrefix.env")
  override lazy val desToken: String = configuration.getString(s"$desServicePrefix.token")
}

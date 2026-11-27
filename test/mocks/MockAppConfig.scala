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

import config.AppConfig
import org.mockito.Mockito.{mock, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.TestSuite

trait MockAppConfig {
  this: TestSuite =>

  val mockAppConfig: AppConfig = mock(classOf[AppConfig])

  object MockedAppConfig {

    def desBaseUrl(): OngoingStubbing[String] =
      when(mockAppConfig.desBaseUrl())

    def desEnvironment(): OngoingStubbing[String] =
      when(mockAppConfig.desEnvironment())

    def desToken(): OngoingStubbing[String] =
      when(mockAppConfig.desToken())

  }

}

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

import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings

val appName = "state-pension-calculation"


ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "2.13.16"

lazy val plugins : Seq[Plugins] = Seq.empty

lazy val microservice = Project(appName, file("."))
  .enablePlugins(Seq(play.sbt.PlayScala, SbtDistributablesPlugin)++ plugins:_*)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always,
    scalacOptions += "-Wconf:cat=unused-imports&src=routes/.*:s"
  )
  .configs(IntegrationTest)
  .settings(integrationTestSettings(): _*)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(CodeCoverageSettings.settings: _*)
  .settings(isPublicArtefact := true)
  .settings(PlayKeys.playDefaultPort := 9790)


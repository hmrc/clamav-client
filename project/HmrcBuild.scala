/*
 * Copyright 2016 HM Revenue & Customs
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
import play.core.PlayVersion
import sbt.Keys._
import sbt._
import scoverage.ScoverageKeys
import uk.gov.hmrc.versioning.SbtGitVersioning
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

object HmrcBuild extends Build {

  import uk.gov.hmrc.DefaultBuildSettings._
  import uk.gov.hmrc._

  val appName = "clamav-client"

  lazy val scoverageSettings = {
    Seq(
      // Semicolon-separated list of regexs matching classes to exclude
      ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;.*AuthService.*;models/.data/..*;view.*",
      ScoverageKeys.coverageExcludedFiles := ".*/frontendGlobal.*;.*/frontendAppConfig.*;.*/frontendWiring.*;.*/views/.*_template.*;.*/govuk_wrapper.*;.*/routes_routing.*;.*/BuildInfo.*",
      // Minimum is deliberately low to avoid failures initially - please increase as we add more coverage
      ScoverageKeys.coverageMinimum := 25,
      ScoverageKeys.coverageFailOnMinimum := false,
      ScoverageKeys.coverageHighlighting := true,
      parallelExecution in Test := false
    )
  }

  def itFilter(name: String): Boolean = !(name endsWith "ISpec")

  lazy val microservice = Project(appName, file("."))
    .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory)
    .settings(majorVersion := 7)
    .settings(
      name := appName,
      targetJvm := "jvm-1.8",
      scalaVersion := "2.12.12",
      libraryDependencies ++= AppDependencies(),
      testOptions in Test := Seq(Tests.Filter(itFilter))
    )
    .settings(scoverageSettings: _*)
}

private object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"       %% "logback-json-logger" % "4.3.0",
    "com.typesafe.play" %% "play"                % PlayVersion.current,
    // force dependencies due to security flaws found in jackson-databind < 2.9.x using XRay
    "com.fasterxml.jackson.core"     % "jackson-core"            % "2.9.8",
    "com.fasterxml.jackson.core"     % "jackson-databind"        % "2.9.8",
    "com.fasterxml.jackson.core"     % "jackson-annotations"     % "2.9.8",
    "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8"   % "2.9.8",
    "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.9.8"
  )

  val test = Seq(
    "org.scalatest" %% "scalatest" % "3.0.5" % "test",
    "org.pegdown"   % "pegdown"    % "1.6.0" % "test"
  )

  def apply() = compile ++ test
}

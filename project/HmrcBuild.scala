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
import sbt.Keys._
import sbt._
import scoverage.ScoverageKeys
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning


object HmrcBuild extends Build {

  import uk.gov.hmrc.DefaultBuildSettings._

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

  def itFilter(name: String): Boolean = ! (name endsWith "ISpec")

  lazy val microservice = Project(appName, file("."))
    .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
    .settings(
      name := appName,
      targetJvm := "jvm-1.8",
      scalaVersion := "2.11.8",
      libraryDependencies ++= AppDependencies(),
      testOptions in Test := Seq(Tests.Filter(itFilter)),
      resolvers := Seq(
          Resolver.bintrayRepo("hmrc", "releases"),
          "typesafe-releases" at "http://repo.typesafe.com/typesafe/releases/"
      )
    ).settings(scoverageSettings: _*)
}

private object AppDependencies {

  val compile =  Seq(
    "uk.gov.hmrc" %% "play-json-logger" % "2.1.1",
    "uk.gov.hmrc" %% "play-authorisation" % "3.1.0"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = ???
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "org.scalatest" %% "scalatest" % "2.2.4" % scope,
        "uk.gov.hmrc" %% "hmrctest" % "1.4.0" % scope,
        "org.pegdown" % "pegdown" % "1.5.0" % scope
      )
    }.test
  }

  def apply() = compile ++ Test()
}

import sbt.Keys._
import sbt._
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning


object HmrcBuild extends Build {

  import uk.gov.hmrc.DefaultBuildSettings._

  val appName = "clamav-client"

  lazy val microservice = Project(appName, file("."))
    .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
    .settings(
      name := appName,
      targetJvm := "jvm-1.8",
      libraryDependencies ++= AppDependencies(),
      resolvers := Seq(
          Resolver.bintrayRepo("hmrc", "releases"),
          "typesafe-releases" at "http://repo.typesafe.com/typesafe/releases/"
      )
    )
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

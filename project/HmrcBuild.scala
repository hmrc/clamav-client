import sbt.Keys._
import sbt._
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning


object HmrcBuild extends Build {

  import BuildDependencies._
  import uk.gov.hmrc.DefaultBuildSettings._

  val appName = "clamav-stream"

  lazy val microservice = Project(appName, file("."))
    .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
    .settings(
      name := appName,
      targetJvm := "jvm-1.7",
      libraryDependencies ++= Seq(
        "uk.gov.hmrc" %% "play-json-logger" % "2.1.1",
        "uk.gov.hmrc" %% "play-authorisation" % "3.1.0",
        Test.scalaTest,
        Test.hmrcTest,
        Test.pegdown
      ),
      Developers()
    )
}

private object BuildDependencies {

  object Compile {
  }

  sealed abstract class Test(scope: String) {
    val scalaTest = "org.scalatest" %% "scalatest" % "2.2.4" % scope
    val hmrcTest = "uk.gov.hmrc" %% "hmrctest" % "1.4.0" % scope
    val pegdown = "org.pegdown" % "pegdown" % "1.5.0" % scope
  }

  object Test extends Test("test")

}

object Developers {

  def apply() = developers := List[Developer]()
}

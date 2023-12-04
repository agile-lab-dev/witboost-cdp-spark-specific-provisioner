import sbt._

trait Dependencies {
  lazy val scalaTestVrs = "3.1.0"
  lazy val scalaMockVrs = "4.4.0"
  lazy val cronUtilsVrs = "9.1.6"
  lazy val cronUtils    = "com.cronutils"  % "cron-utils" % cronUtilsVrs
  lazy val scalaTest    = "org.scalatest" %% "scalatest"  % scalaTestVrs % "test"
  lazy val scalaMock    = "org.scalamock" %% "scalamock"  % scalaMockVrs % "test"

  lazy val testDependencies = Seq(
    scalaTest,
    scalaMock
  )

  lazy val scalaCommonVrs  = "0.0.0-SNAPSHOT-94c691a.wit-365-cdp-common-l"
  lazy val s3Gat           = "it.agilelab.provisioning" %% "scala-mesh-aws-s3"              % scalaCommonVrs
  lazy val secretsGat      = "it.agilelab.provisioning" %% "scala-mesh-aws-secrets"         % scalaCommonVrs
  lazy val cdpDlComm       = "it.agilelab.provisioning" %% "scala-mesh-cdp-dl"              % scalaCommonVrs
  lazy val cdpDeComm       = "it.agilelab.provisioning" %% "scala-mesh-cdp-de"              % scalaCommonVrs
  lazy val meshComm        = "it.agilelab.provisioning" %% "scala-mesh-self-service"        % scalaCommonVrs
  lazy val meshCommLambda  = "it.agilelab.provisioning" %% "scala-mesh-self-service-lambda" % scalaCommonVrs
  lazy val scalaMeshCdpEnv = "it.agilelab.provisioning" %% "scala-mesh-cdp-env"             % scalaCommonVrs

  lazy val provisioningDependencies = Seq(
    s3Gat,
    secretsGat,
    cdpDlComm,
    cdpDeComm,
    meshComm,
    meshCommLambda,
    scalaMeshCdpEnv
  )

  private val http4sVersion                  = "0.23.18"
  lazy val http4sDependencies: Seq[ModuleID] = Seq(
    "org.http4s" %% "http4s-ember-client",
    "org.http4s" %% "http4s-ember-server",
    "org.http4s" %% "http4s-dsl",
    "org.http4s" %% "http4s-circe"
  ).map(_ % http4sVersion)

  private val circeVersion                   = "0.14.5"
  lazy val circeDependencies: Seq[ModuleID]  = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % circeVersion) ++ Seq("io.circe" %% "circe-generic-extras" % "0.14.3")

}

object Dependencies extends Dependencies

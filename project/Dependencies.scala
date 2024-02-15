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

  lazy val scalaMeshCoreVersion     = "0.0.0-SNAPSHOT-15dfac13ba.wit-1430-scala-mesh"
  lazy val provisioningDependencies = Seq(
    "it.agilelab.provisioning" %% "scala-mesh-aws-s3"                     % scalaMeshCoreVersion,
    "it.agilelab.provisioning" %% "scala-mesh-aws-secrets"                % scalaMeshCoreVersion,
    "it.agilelab.provisioning" %% "scala-mesh-aws-lambda-handlers"        % scalaMeshCoreVersion,
    "it.agilelab.provisioning" %% "scala-mesh-cdp-de"                     % scalaMeshCoreVersion,
    "it.agilelab.provisioning" %% "scala-mesh-cdp-dl"                     % scalaMeshCoreVersion,
    "it.agilelab.provisioning" %% "scala-mesh-cdp-env"                    % scalaMeshCoreVersion,
    "it.agilelab.provisioning" %% "scala-mesh-self-service"               % scalaMeshCoreVersion,
    "it.agilelab.provisioning" %% "scala-mesh-self-service-lambda"        % scalaMeshCoreVersion,
    "it.agilelab.provisioning" %% "scala-mesh-principals-mapping"         % scalaMeshCoreVersion,
    "it.agilelab.provisioning" %% "scala-mesh-principals-mapping-samples" % scalaMeshCoreVersion
  )

  private val http4sVersion                         = "0.23.18"
  lazy val http4sDependencies: Seq[ModuleID]        = Seq(
    "org.http4s" %% "http4s-ember-client",
    "org.http4s" %% "http4s-ember-server",
    "org.http4s" %% "http4s-dsl",
    "org.http4s" %% "http4s-circe"
  ).map(_ % http4sVersion)

  private val circeVersion                          = "0.14.5"
  lazy val circeDependencies: Seq[ModuleID]         = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % circeVersion) ++ Seq("io.circe" %% "circe-generic-extras" % "0.14.3")

  lazy val quartzAndLivyDependencies: Seq[ModuleID] = Seq(
    "org.apache.livy"           % "livy-client-http" % "0.8.0-incubating",
    "org.apache.spark"         %% "spark-core"       % "3.2.4",
    "org.scalaj"               %% "scalaj-http"      % "2.4.2",
    "org.quartz-scheduler"      % "quartz"           % "2.3.2",
    "ch.qos.logback"            % "logback-classic"  % "1.2.3",
    "org.postgresql"            % "postgresql"       % "42.2.5",
    "org.apache.httpcomponents" % "httpclient"       % "4.5.13"
  )
}

object Dependencies extends Dependencies

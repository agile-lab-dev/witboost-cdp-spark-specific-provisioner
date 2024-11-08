import sbtassembly.PathList
import wartremover.WartRemover.autoImport.Wart
import sbt.Keys.{ csrConfiguration, updateClassifiers, updateSbtClassifiers }
import lmcoursier.definitions.Authentication
import Settings._

inThisBuild(
  Seq(
    organization := "it.agilelab.provisioning",
    scalaVersion := "2.13.2",
    version := ComputeVersion.version
  )
)

lazy val root = (project in file("."))
  .settings(
    name := "spark-workloads-provisioner",
    mainClass in Compile := Some("it.agilelab.provisioning.spark.workloads.provisioner.app.Main"),
    artifactorySettings,
    dockerBuildOptions ++= Seq("--network=host"),
    dockerBaseImage := "openjdk:11-buster",
    dockerUpdateLatest := true,
    daemonUser := "daemon",
    Docker / version := (ThisBuild / version).value,
    Docker / packageName :=
      s"registry.gitlab.com/agilefactory/witboost.mesh/provisioning/cdp-refresh/witboost.mesh.provisioning.workload.cdp.spark",
    Docker / dockerExposedPorts := Seq(8093)
  )
  .enablePlugins(JavaAppPackaging)
  .aggregate(
    core,
    service,
    api,
    quartzScheduler
  )
  .dependsOn(
    core,
    service,
    api,
    quartzScheduler
  )

lazy val core = (project in file("core"))
  .settings(
    name := "spark-workloads-provisioner-core",
    libraryDependencies ++= Dependencies.testDependencies ++ Seq(
    ) ++ Dependencies.provisioningDependencies,
    libraryDependencies ++= Dependencies.http4sDependencies,
    libraryDependencies ++= Dependencies.jsonDependencies,
    artifactorySettings,
    commonAssemblySettings,
    wartremoverSettings,
    k8tyGitlabPluginSettings
  )
  .enablePlugins(K8tyGitlabPlugin)

lazy val service = (project in file("service"))
  .settings(
    name := "spark-workloads-provisioner-service",
    libraryDependencies ++= Dependencies.testDependencies,
    artifactorySettings,
    commonAssemblySettings,
    wartremoverSettings,
    k8tyGitlabPluginSettings
  )
  .dependsOn(core, quartzScheduler)
  .enablePlugins(K8tyGitlabPlugin)

lazy val api = (project in file("api"))
  .settings(
    name := "spark-workloads-provisioner-api",
    libraryDependencies ++= Dependencies.testDependencies ++ Seq(
      Dependencies.cronUtils
    ) ++ Dependencies.http4sDependencies ++ Dependencies.circeDependencies,
    artifactorySettings,
    commonAssemblySettings,
    wartremoverSettings,
    k8tyGitlabPluginSettings
  )
  .settings(
    Compile / guardrailTasks := GuardrailHelpers.createGuardrailTasks((Compile / sourceDirectory).value / "openapi") {
      openApiFile =>
        List(
          ScalaServer(
            openApiFile.file,
            pkg = "it.agilelab.provisioning.api.generated",
            framework = "http4s",
            tracing = false
          )
        )
    },
    coverageExcludedPackages := "it.agilelab.provisioning.api.generated.*"
  )
  .dependsOn(service)
  .enablePlugins(K8tyGitlabPlugin)

lazy val quartzScheduler = (project in file("quartz-embedded-scheduler"))
  .settings(
    name := "spark-workloads-provisioner-quartz-embedded-scheduler",
    libraryDependencies ++= Dependencies.testDependencies,
    libraryDependencies ++= Dependencies.quartzAndLivyDependencies,
    libraryDependencies ++= Dependencies.provisioningDependencies,
    artifactorySettings,
    commonAssemblySettings,
    wartremoverSettings,
    k8tyGitlabPluginSettings
  )
  .dependsOn(core)
  .enablePlugins(K8tyGitlabPlugin)

lazy val commonAssemblySettings = Seq(
  assemblyJarName in assembly := s"${name.value}-${version.value}.jar",
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", "MANIFEST.MF")                     => MergeStrategy.discard
    case PathList("META-INF", xs @ _*)                           => MergeStrategy.last
    case PathList("org", "apache", "commons", "logging", x @ _*) => MergeStrategy.last
    case PathList("mime.types")                                  => MergeStrategy.last
    case "NOTICE"                                                => MergeStrategy.discard
    case "LICENSE"                                               => MergeStrategy.discard
    case _                                                       => MergeStrategy.last
  }
)

lazy val wartremoverSettings = Seq(
  wartremoverErrors in (Compile, compile) ++= Warts.allBut(
    Wart.Nothing,
    Wart.TraversableOps,
    Wart.StringPlusAny,
    Wart.ToString,
    Wart.AsInstanceOf,
    Wart.Any,
    Wart.DefaultArguments
  ),
  wartremoverExcluded += sourceManaged.value
)

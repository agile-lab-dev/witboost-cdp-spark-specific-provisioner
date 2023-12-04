import sbtassembly.PathList
import wartremover.WartRemover.autoImport.Wart
import sbt.Keys.{ csrConfiguration, updateClassifiers, updateSbtClassifiers }
import lmcoursier.definitions.Authentication

inThisBuild(
  Seq(
    organization := "it.agilelab.provisioning",
    scalaVersion := "2.13.2",
    version := "0.9.1"
  )
)

lazy val root = (project in file("."))
  .settings(
    name := "spark-workloads-provisioner",
    artifactorySettings,
    publish / skip := true
  )
  .disablePlugins(AssemblyPlugin)
  .aggregate(
    core,
    service,
    api
  )
  .dependsOn(
    core,
    service,
    api
  )

lazy val core = (project in file("core"))
  .settings(
    name := "spark-workloads-provisioner-core",
    libraryDependencies ++= Dependencies.testDependencies ++ Seq(
    ) ++ Dependencies.provisioningDependencies,
    libraryDependencies ++= Dependencies.http4sDependencies,
    artifactorySettings,
    commonAssemblySettings,
    wartremoverSettings
  )

lazy val service = (project in file("service"))
  .settings(
    name := "spark-workloads-provisioner-service",
    libraryDependencies ++= Dependencies.testDependencies,
    artifactorySettings,
    commonAssemblySettings,
    wartremoverSettings
  )
  .dependsOn(core)

lazy val api = (project in file("api"))
  .settings(
    name := "spark-workloads-provisioner-api",
    libraryDependencies ++= Dependencies.testDependencies ++ Seq(
      Dependencies.cronUtils
    ) ++ Dependencies.http4sDependencies ++ Dependencies.circeDependencies,
    artifactorySettings,
    commonAssemblySettings,
    wartremoverSettings
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

lazy val artifactorySettings = Seq(
  csrConfiguration ~=
    (configuration =>
      configuration.addRepositoryAuthentication(
        "gitlab",
        Authentication(
          sys.env.getOrElse("GITLAB_ARTIFACT_USER", "GITLAB_ARTIFACT_USER"),
          sys.env.getOrElse("GITLAB_ARTIFACT_TOKEN", "GITLAB_ARTIFACT_TOKEN")
        )
      )
    ),
  updateClassifiers / csrConfiguration := csrConfiguration.value,
  updateSbtClassifiers / csrConfiguration := csrConfiguration.value,
  resolvers ++= Seq(
    ExternalResolvers.gitlabScalaMeshCommonsResolver,
    ExternalResolvers.clouderaResolver
  )
)

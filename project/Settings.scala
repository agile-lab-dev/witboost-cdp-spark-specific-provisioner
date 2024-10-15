import lmcoursier.definitions.Authentication
import sbt.Keys._
import sbt._
import wartremover.WartRemover.autoImport._

object Settings {
  lazy val k8tyGitlabPluginSettings = Seq(
    app.k8ty.sbt.gitlab.K8tyGitlabPlugin.gitlabProjectId := "51427986"
  )

  lazy val wartremoverSettings = Seq(
    wartremoverErrors in (Compile, compile) ++= Warts.allBut(
      Wart.Any,
      Wart.Nothing,
      Wart.Serializable,
      Wart.JavaSerializable,
      Wart.NonUnitStatements,
      Wart.ImplicitParameter,
      Wart.Throw,
      Wart.StringPlusAny,
      Wart.ToString,
      Wart.DefaultArguments,
      Wart.AsInstanceOf,
      Wart.Equals
    ),
    wartremoverExcluded += sourceManaged.value
  )

  lazy val artifactorySettings = Seq(
    resolvers ++= Seq(
      ExternalResolvers.clouderaResolver
    )
  )

}

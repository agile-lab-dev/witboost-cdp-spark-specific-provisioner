package it.agilelab.provisioning.spark.workload.core

import cats.syntax.functor._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{ Decoder, Encoder }

sealed trait SparkCde extends Product with Serializable
object SparkCde {

  final case class SparkCdeJob(
    cdeService: String,
    cdeCluster: String,
    jobName: String,
    jar: String,
    className: String,
    jobConfig: Option[JobConfig]
  ) extends SparkCde

  final case class PySparkCdeJob(
    cdeService: String,
    cdeCluster: String,
    jobName: String,
    pythonFile: String,
    pythonVersion: String,
    jobConfig: Option[JobConfig]
  ) extends SparkCde

  final case class PySparkCdeJobWithPyEnv(
    cdeService: String,
    cdeCluster: String,
    jobName: String,
    pythonFile: String,
    pythonVersion: String,
    requirementsFile: String,
    pyMirror: Option[String],
    jobConfig: Option[JobConfig]
  ) extends SparkCde

  implicit class SparkCdeOps(sparkCde: SparkCde) {

    def cdeService: String = sparkCde match {
      case SparkCdeJob(srv, _, _, _, _, _)                  => srv
      case PySparkCdeJob(srv, _, _, _, _, _)                => srv
      case PySparkCdeJobWithPyEnv(srv, _, _, _, _, _, _, _) => srv
    }

    def cdeCluster: String = sparkCde match {
      case SparkCdeJob(_, cluster, _, _, _, _)                  => cluster
      case PySparkCdeJob(_, cluster, _, _, _, _)                => cluster
      case PySparkCdeJobWithPyEnv(_, cluster, _, _, _, _, _, _) => cluster
    }

    def jobName: String = sparkCde match {
      case SparkCdeJob(_, _, jobName, _, _, _)                  => jobName
      case PySparkCdeJob(_, _, jobName, _, _, _)                => jobName
      case PySparkCdeJobWithPyEnv(_, _, jobName, _, _, _, _, _) => jobName
    }

    def appFile: String = sparkCde match {
      case SparkCdeJob(_, _, _, jar, _, _)                     => jar
      case PySparkCdeJob(_, _, _, pyFile, _, _)                => pyFile
      case PySparkCdeJobWithPyEnv(_, _, _, pyFile, _, _, _, _) => pyFile
    }

    def entryPoint: Option[String] = sparkCde match {
      case SparkCdeJob(_, _, _, _, cn, _) => Some(cn)
      case _                              => None
    }

    def pyVersion: Option[String] = sparkCde match {
      case PySparkCdeJob(_, _, _, _, pyV, _)                => Some(pyV)
      case PySparkCdeJobWithPyEnv(_, _, _, _, pyV, _, _, _) => Some(pyV)
      case _                                                => None
    }

    def jobConfig: Option[JobConfig] = sparkCde match {
      case SparkCdeJob(_, _, _, _, _, jobConf)                  => jobConf
      case PySparkCdeJob(_, _, _, _, _, jobConf)                => jobConf
      case PySparkCdeJobWithPyEnv(_, _, _, _, _, _, _, jobConf) => jobConf
    }
  }

  implicit val decodeSparkCdeJob: Decoder[SparkCde] =
    List[Decoder[SparkCde]](
      Decoder[PySparkCdeJobWithPyEnv].widen,
      Decoder[PySparkCdeJob].widen,
      Decoder[SparkCdeJob].widen
    ).reduceLeft(_ or _)

  implicit val encodeSparkCdeJob: Encoder[SparkCde] = Encoder.instance {
    case j: SparkCdeJob            => j.asJson
    case j: PySparkCdeJob          => j.asJson
    case j: PySparkCdeJobWithPyEnv => j.asJson
  }

}

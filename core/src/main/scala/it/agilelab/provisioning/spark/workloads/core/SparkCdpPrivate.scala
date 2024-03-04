package it.agilelab.provisioning.spark.workloads.core

import cats.syntax.functor._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{ Decoder, Encoder }

sealed trait SparkCdpPrivate extends Product with Serializable

object SparkCdpPrivate {

  final case class SparkCdpPrivateJob(
    jobName: String,
    jar: String,
    className: String,
    jobConfig: Option[JobConfig],
    queue: String
  ) extends SparkCdpPrivate

  implicit class SparkCdpPrivateOps(SparkCdpPrivate: SparkCdpPrivate) {
    def jobName: String = SparkCdpPrivate match {
      case SparkCdpPrivateJob(jobName, _, _, _, _) => jobName
    }

    def appFile: String = SparkCdpPrivate match {
      case SparkCdpPrivateJob(_, jar, _, _, _) => jar
    }

    def className: String            = SparkCdpPrivate match {
      case SparkCdpPrivateJob(_, _, className, _, _) => className
    }
    def jobConfig: Option[JobConfig] = SparkCdpPrivate match {
      case SparkCdpPrivateJob(_, _, _, jobConf, _) => jobConf
    }

    def queue: String = SparkCdpPrivate match {
      case SparkCdpPrivateJob(_, _, _, _, queue) => queue
    }
  }

  implicit val decodeSparkCdpPrivateJob: Decoder[SparkCdpPrivate] =
    List[Decoder[SparkCdpPrivate]](
      Decoder[SparkCdpPrivateJob].widen
    ).reduceLeft(_ or _)

  implicit val encodeSparkCdpPrivateJob: Encoder[SparkCdpPrivate] = Encoder.instance { case j: SparkCdpPrivateJob =>
    j.asJson
  }

}

package it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.artifactory.s3

import cats.implicits._
import it.agilelab.provisioning.aws.s3.gateway.S3Gateway
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.artifactory.ArtifactoryGatewayError.{
  S3ArtefactError,
  WronglyFormattedPathError
}
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.artifactory.{
  ArtifactoryGateway,
  ArtifactoryGatewayError
}

class S3ArtifactoryGateway(s3Gateway: S3Gateway) extends ArtifactoryGateway {

  override def get(location: String): Either[ArtifactoryGatewayError, Array[Byte]] =
    location match {
      case s"s3://$bucket/$key" => s3Gateway.getObjectContent(bucket, key).leftMap(e => S3ArtefactError(bucket, key, e))
      case _                    => Left(WronglyFormattedPathError(location))
    }
}

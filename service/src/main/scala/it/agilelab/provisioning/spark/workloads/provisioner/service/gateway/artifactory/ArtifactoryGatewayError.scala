package it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.artifactory

import cats.Show
import cats.implicits._
import it.agilelab.provisioning.aws.s3.gateway.S3GatewayError

sealed trait ArtifactoryGatewayError extends Exception with Product with Serializable

object ArtifactoryGatewayError {
  final case class S3ArtefactError(bucket: String, key: String, error: S3GatewayError) extends ArtifactoryGatewayError
  final case class WronglyFormattedPathError(path: String)                             extends ArtifactoryGatewayError

  implicit def showArtifactoryGatewayError: Show[ArtifactoryGatewayError] = Show.show {
    case S3ArtefactError(bucket, key, error) => show"S3ArtefactError($bucket,$key,$error)"
    case WronglyFormattedPathError(path)     => show"WronglyFormattedPathError($path)"
  }
}

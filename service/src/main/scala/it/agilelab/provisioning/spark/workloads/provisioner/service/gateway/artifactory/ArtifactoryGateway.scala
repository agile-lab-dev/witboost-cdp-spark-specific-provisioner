package it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.artifactory

import it.agilelab.provisioning.aws.s3.gateway.S3Gateway
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.artifactory.s3.S3ArtifactoryGateway

trait ArtifactoryGateway {
  def get(location: String): Either[ArtifactoryGatewayError, Array[Byte]]
}

object ArtifactoryGateway {

  def create(gateway: S3Gateway): ArtifactoryGateway =
    new S3ArtifactoryGateway(gateway)
}

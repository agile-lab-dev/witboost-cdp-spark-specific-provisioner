package it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.artifactory.s3

import it.agilelab.provisioning.commons.audit.Audit
import cats.implicits._
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.artifactory.{
  ArtifactoryGateway,
  ArtifactoryGatewayError
}

class S3ArtifactoryGatewayWithAudit(artifactoryGateway: ArtifactoryGateway, audit: Audit) extends ArtifactoryGateway {

  override def get(location: String): Either[ArtifactoryGatewayError, Array[Byte]] = {
    val action = s"Get($location)"
    audit.info(s"Executing $action")
    val result = artifactoryGateway.get(location)
    result match {
      case Right(_) => audit.info(s"Retrieve artifact successfully executed")
      case Left(l)  => audit.error(show"Retrieve artifact failed. Details: $l")
    }
    result
  }
}

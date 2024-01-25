package it.agilelab.provisioning.spark.workloads.provisioner.app.service.gateway.artifactory

import it.agilelab.provisioning.aws.s3.gateway.S3GatewayError.GetObjectContentErr
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.artifactory.ArtifactoryGatewayError
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.artifactory.ArtifactoryGatewayError.S3ArtefactError
import org.scalatest.EitherValues._

trait ArtifactoryGatewayTestSupport {
  def assertS3ArtefactError[A](actual: Either[ArtifactoryGatewayError, A], error: S3ArtefactError): Unit = {
    assert(actual.isLeft)
    assert(actual.left.value.isInstanceOf[S3ArtefactError])
    assert(actual.left.value.asInstanceOf[S3ArtefactError].bucket == error.bucket)
    assert(actual.left.value.asInstanceOf[S3ArtefactError].key == error.key)
    assert(actual.left.value.asInstanceOf[S3ArtefactError].error.isInstanceOf[GetObjectContentErr])
    assert(
      actual.left.value.asInstanceOf[S3ArtefactError].error.asInstanceOf[GetObjectContentErr].bucket
        == error.error.asInstanceOf[GetObjectContentErr].bucket
    )
    assert(
      actual.left.value.asInstanceOf[S3ArtefactError].error.asInstanceOf[GetObjectContentErr].key
        == error.error.asInstanceOf[GetObjectContentErr].key
    )
    assert(
      actual.left.value.asInstanceOf[S3ArtefactError].error.asInstanceOf[GetObjectContentErr].error.getMessage
        == error.error.asInstanceOf[GetObjectContentErr].error.getMessage
    )
  }
}

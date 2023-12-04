package it.agilelab.provisioning.spark.workloads.provisioner.app.service.gateway.artifactory

import it.agilelab.provisioning.aws.s3.gateway.S3GatewayError.GetObjectContentErr
import it.agilelab.provisioning.commons.audit.Audit
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.artifactory.ArtifactoryGatewayError.S3ArtefactError
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.artifactory.ArtifactoryGateway
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.artifactory.s3.S3ArtifactoryGatewayWithAudit
import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite

class S3ArtifactoryGatewayWithAuditTest extends AnyFunSuite with MockFactory with ArtifactoryGatewayTestSupport {

  val audit: Audit                               = mock[Audit]
  val baseArtifactoryGateway: ArtifactoryGateway = mock[ArtifactoryGateway]

  val artifactoryGateway = new S3ArtifactoryGatewayWithAudit(baseArtifactoryGateway, audit)

  test("get logs info") {
    (audit
      .info(_: String))
      .expects("Executing Get(s3://bucket/path/to/object.txt)")
      .once()
    (baseArtifactoryGateway.get _)
      .expects("s3://bucket/path/to/object.txt")
      .once()
      .returns(Right("fileContent".getBytes))
    (audit
      .info(_: String))
      .expects("Retrieve artifact successfully executed")
      .once()

    val actual   = artifactoryGateway.get("s3://bucket/path/to/object.txt")
    val expected = Right("fileContent".getBytes)

    assert(actual.getOrElse(fail()) sameElements expected.getOrElse(fail()))
  }

  test("get logs error on Left") {
    (audit
      .info(_: String))
      .expects(
        "Executing Get(s3://bucket/path/to/object.txt)"
      )
      .once()
    (baseArtifactoryGateway.get _)
      .expects("s3://bucket/path/to/object.txt")
      .once()
      .returns(
        Left(
          S3ArtefactError(
            "bucket",
            "path/to/object.txt",
            GetObjectContentErr("bucket", "path/to/object.txt", new IllegalArgumentException("x"))
          )
        )
      )
    (audit
      .error(_: String))
      .expects(where { s: String =>
        s.startsWith(
          "Retrieve artifact failed. Details: S3ArtefactError(bucket,path/to/object.txt,GetObjectContentErr(bucket,path/to/object.txt,java.lang.IllegalArgumentException: x"
        )
      })
      .once()

    val actual = artifactoryGateway.get("s3://bucket/path/to/object.txt")

    assertS3ArtefactError(
      actual,
      S3ArtefactError(
        "bucket",
        "path/to/object.txt",
        GetObjectContentErr("bucket", "path/to/object.txt", new IllegalArgumentException("x"))
      )
    )
  }
}

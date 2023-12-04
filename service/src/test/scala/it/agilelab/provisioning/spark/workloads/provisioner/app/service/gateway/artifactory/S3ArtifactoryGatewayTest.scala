package it.agilelab.provisioning.spark.workloads.provisioner.app.service.gateway.artifactory

import it.agilelab.provisioning.aws.s3.gateway.S3Gateway
import it.agilelab.provisioning.aws.s3.gateway.S3GatewayError.GetObjectContentErr
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.artifactory.ArtifactoryGatewayError.{
  S3ArtefactError,
  WronglyFormattedPathError
}
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.artifactory.s3.S3ArtifactoryGateway
import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite

class S3ArtifactoryGatewayTest extends AnyFunSuite with MockFactory with ArtifactoryGatewayTestSupport {

  val s3Gateway: S3Gateway = mock[S3Gateway]
  val artifactoryGateway   = new S3ArtifactoryGateway(s3Gateway)

  test("get return Right") {
    (s3Gateway.getObjectContent _)
      .expects("bucket", "path/to/object.txt")
      .once()
      .returns(Right("fileContent".getBytes))

    val actual   = artifactoryGateway.get("s3://bucket/path/to/object.txt")
    val expected = Right("fileContent".getBytes)

    assert(actual.getOrElse(fail()) sameElements expected.getOrElse(fail()))
  }

  test("get return Left(S3ArtefactError)") {
    (s3Gateway.getObjectContent _)
      .expects("bucket", "path/to/object.txt")
      .once()
      .returns(Left(GetObjectContentErr("bucket", "path/to/object.txt", new IllegalArgumentException("x"))))

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

  test("get return Left(WronglyFormattedPathError)") {
    val actual   = artifactoryGateway.get("wrongly formatted path")
    val expected = Left(WronglyFormattedPathError("wrongly formatted path"))
    assert(actual == expected)
  }
}

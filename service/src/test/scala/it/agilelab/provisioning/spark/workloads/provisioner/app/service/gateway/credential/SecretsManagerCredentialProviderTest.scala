package it.agilelab.provisioning.spark.workloads.provisioner.app.service.gateway.credential

import it.agilelab.provisioning.aws.secrets.gateway.SecretsGateway
import it.agilelab.provisioning.aws.secrets.gateway.model.AwsSecret
import it.agilelab.provisioning.commons.http.Auth.BasicCredential
import it.agilelab.provisioning.mesh.repository.Repository
import it.agilelab.provisioning.mesh.self.service.lambda.core.model.Domain
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.credential.SecretsManagerCredentialsProvider
import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite

class SecretsManagerCredentialProviderTest extends AnyFunSuite with MockFactory {

  test("get") {
    val repo           = mock[Repository[Domain, String, Unit]]
    val secretsGateway = mock[SecretsGateway]

    (repo.findById _)
      .expects("domain")
      .returns(Right(Some(Domain("domain", "dm"))))
    (secretsGateway.get _)
      .expects("sdp/dm-my-dp-role-workload")
      .returns(Right(Some(AwsSecret("arn", "name", "{\"username\":\"my-user\",\"password\":\"my-pwd\"}"))))

    val actual   = new SecretsManagerCredentialsProvider(repo, secretsGateway).get("domain", "my-dp")
    val expected = Right(BasicCredential("my-user", "my-pwd"))
    assert(actual == expected)
  }
}

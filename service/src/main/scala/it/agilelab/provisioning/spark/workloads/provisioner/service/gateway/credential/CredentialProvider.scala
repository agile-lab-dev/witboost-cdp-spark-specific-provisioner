package it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.credential

import it.agilelab.provisioning.aws.secrets.gateway.SecretsGateway
import it.agilelab.provisioning.commons.http.Auth.BasicCredential
import it.agilelab.provisioning.mesh.repository.Repository
import it.agilelab.provisioning.mesh.self.service.lambda.core.model.Domain

trait CredentialProvider {
  def get(domainShortName: String, dataProduct: String): Either[CredentialProviderError, BasicCredential]
}

object CredentialProvider {

  def create(
    domainRepository: Repository[Domain, String, Unit],
    secretGateway: SecretsGateway
  ): CredentialProvider =
//    new SecretsManagerCredentialsProvider(domainRepository, secretGateway)
    new CredentialProviderWithEnvCredentials()
}

package it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.clients

import cats.implicits._
import it.agilelab.provisioning.commons.client.cdp.de.cluster.CdeClusterClient
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.credential.{
  CredentialProvider,
  CredentialProviderError
}
import CredentialProviderError.CredentialProviderInitErr

class CdeClientFactory(credentialProvider: CredentialProvider) {
  def create(domain: String, dataProduct: String): Either[CredentialProviderError, CdeClusterClient] =
    for {
      basicCreds <- credentialProvider.get(domain, dataProduct)
      client     <- CdeClusterClient.defaultWithAudit(basicCreds).leftMap(CredentialProviderInitErr(_))
    } yield client

}

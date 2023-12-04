package it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.credential
import it.agilelab.provisioning.commons.http.Auth
import it.agilelab.provisioning.commons.http.Auth.BasicCredential
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.credential.CredentialProviderError._

class CredentialProviderWithEnvCredentials extends CredentialProvider {
  override def get(
    domainShortName: String,
    dataProduct: String
  ): Either[CredentialProviderError, Auth.BasicCredential] = {
    val credentials = for {
      user <- sys.env.get("BASIC_CRED_USER")
      pass <- sys.env.get("BASIC_CRED_PASS")

    } yield BasicCredential(user, pass)

    credentials.toRight(CredentialNotFoundErr("Credentials not found."))

  }
}

package it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.credential

import cats.implicits._
import io.circe.generic.auto._
import it.agilelab.provisioning.aws.secrets.gateway.SecretsGateway
import it.agilelab.provisioning.commons.http.Auth.BasicCredential
import it.agilelab.provisioning.commons.support.ParserSupport
import it.agilelab.provisioning.mesh.repository.Repository
import it.agilelab.provisioning.mesh.self.service.lambda.core.model.Domain
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.credential.CredentialProviderError._

class SecretsManagerCredentialsProvider(
  repository: Repository[Domain, String, Unit],
  secretsGateway: SecretsGateway
) extends CredentialProvider
    with ParserSupport {
  override def get(domain: String, dataProduct: String): Either[CredentialProviderError, BasicCredential] =
    for {
      optDm         <- repository.findById(domain).leftMap(e => CredentialFindErr(domain, dataProduct, e))
      dm            <- optDm.toRight(CredentialDomainNotFoundErr(domain))
      credentialName = s"sdp/${dm.shortName}-$dataProduct-role-workload"
      credsOpt      <- secretsGateway
                         .get(credentialName)
                         .leftMap(e => CredentialSecretFindErr(domain, dataProduct, e))
      creds         <- credsOpt.toRight(CredentialNotFoundErr(credentialName))
      basicCreds    <- fromJson[BasicCredential](creds.value).leftMap(e => CredentialParsingErr(domain, dataProduct, e))
    } yield basicCreds
}

package it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.credential

import cats.Show
import cats.implicits._
import it.agilelab.provisioning.aws.secrets.gateway.SecretsGatewayError
import it.agilelab.provisioning.commons.showable.ShowableOps
import it.agilelab.provisioning.commons.support.ParserError
import it.agilelab.provisioning.mesh.repository.RepositoryError

sealed trait CredentialProviderError extends Exception with Product with Serializable

object CredentialProviderError {

  final case class CredentialProviderInitErr(throwable: Throwable) extends CredentialProviderError

  final case class CredentialFindErr(domain: String, dataProduct: String, error: RepositoryError)
      extends CredentialProviderError

  final case class CredentialDomainNotFoundErr(domain: String) extends CredentialProviderError

  final case class CredentialNotFoundErr(credential: String) extends CredentialProviderError

  final case class CredentialSecretFindErr(domain: String, dataProduct: String, error: SecretsGatewayError)
      extends CredentialProviderError

  final case class CredentialParsingErr(domain: String, dataProduct: String, error: ParserError)
      extends CredentialProviderError

  implicit def showCredentialProviderError: Show[CredentialProviderError] = Show.show {
    case CredentialProviderInitErr(throwable)                =>
      println(throwable.getMessage)
      show"CredentialProviderInitErr(${throwable.getMessage})"
    case CredentialFindErr(domain, dataProduct, error)       =>
      show"CredentialFindError($domain,$dataProduct,$error)"
    case CredentialDomainNotFoundErr(domain)                 =>
      show"CredentialDomainNotFoundErr($domain)"
    case CredentialSecretFindErr(domain, dataProduct, error) =>
      show"CredentialSecretFindError($domain,$dataProduct,$error)"
    case CredentialParsingErr(domain, dataProduct, error)    =>
      show"CredentialParsingError($domain,$dataProduct,$error)"
    case CredentialNotFoundErr(credential)                   =>
      show"CredentialNotFoundErr($credential)"
  }

}

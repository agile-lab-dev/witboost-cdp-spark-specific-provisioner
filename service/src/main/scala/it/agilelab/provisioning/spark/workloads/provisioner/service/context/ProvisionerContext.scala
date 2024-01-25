package it.agilelab.provisioning.spark.workloads.provisioner.service.context

import cats.implicits.toBifunctorOps
import it.agilelab.provisioning.aws.s3.gateway.S3Gateway
import it.agilelab.provisioning.aws.secrets.gateway.SecretsGateway
import it.agilelab.provisioning.commons.client.cdp.de.CdpDeClient
import it.agilelab.provisioning.commons.client.cdp.dl.CdpDlClient
import it.agilelab.provisioning.commons.config.Conf
import it.agilelab.provisioning.mesh.repository.Repository
import it.agilelab.provisioning.mesh.self.service.api.model.ApiResponse.ProvisioningStatus
import it.agilelab.provisioning.mesh.self.service.lambda.core.model.Domain
import it.agilelab.provisioning.spark.workload.core.context.ContextError
import it.agilelab.provisioning.spark.workload.core.context.ContextError._
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.artifactory.ArtifactoryGateway
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.clients.CdeClientFactory
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.credential.CredentialProvider
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.idbroker.IdBrokerHostsProvider
import it.agilelab.provisioning.spark.workloads.provisioner.service.repository.{
  ConfigDomainRepository,
  MemoryStateRepository
}

final case class ProvisionerContext(
  domainRepository: Repository[Domain, String, Unit],
  stateRepo: Repository[ProvisioningStatus, String, Unit],
  cdpDeClient: CdpDeClient,
  idBrokerHostsProvider: IdBrokerHostsProvider,
  artifactoryGateway: ArtifactoryGateway,
  cdeClusterClientFactory: CdeClientFactory
)

object ProvisionerContext {

  def init(conf: Conf): Either[ContextError, ProvisionerContext] =
    for {
      cdpDeClient   <- CdpDeClient.defaultWithAudit().leftMap(e => ClientError("CdpDeClient", e))
      cdpDlClient   <- CdpDlClient.defaultWithAudit().leftMap(e => ClientError("CdpDlClient", e))
      s3Gateway     <- S3Gateway.defaultWithAudit().leftMap(e => ClientError("S3Gateway", e))
      secretGateway <- SecretsGateway.defaultWithAudit().leftMap(e => ClientError("SecretsGateway", e))
      stateRepo      = new MemoryStateRepository()
      domainRepo     = new ConfigDomainRepository(conf)
    } yield ProvisionerContext(
      domainRepo,
      stateRepo,
      cdpDeClient,
      IdBrokerHostsProvider.create(cdpDlClient),
      ArtifactoryGateway.create(s3Gateway),
      new CdeClientFactory(CredentialProvider.create(domainRepo, secretGateway))
    )

}

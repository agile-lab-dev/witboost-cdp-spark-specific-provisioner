package it.agilelab.provisioning.spark.workloads.provisioner.service.context

import it.agilelab.provisioning.commons.config.Conf
import it.agilelab.provisioning.mesh.repository.Repository
import it.agilelab.provisioning.mesh.self.service.api.model.ApiResponse.ProvisioningStatus
import it.agilelab.provisioning.mesh.self.service.lambda.core.model.Domain
import it.agilelab.provisioning.spark.workloads.core.context.ContextError
import it.agilelab.provisioning.spark.workloads.provisioner.service.repository.{
  ConfigDomainRepository,
  MemoryStateRepository
}

final case class ProvisionerContextCdpPrivate(
  domainRepository: Repository[Domain, String, Unit],
  stateRepo: Repository[ProvisioningStatus, String, Unit]
)

object ProvisionerContextCdpPrivate {
  def init(conf: Conf): Either[ContextError, ProvisionerContextCdpPrivate] = {
    val stateRepo  = new MemoryStateRepository()
    val domainRepo = new ConfigDomainRepository(conf)
    Right(ProvisionerContextCdpPrivate(domainRepo, stateRepo))
  }
}

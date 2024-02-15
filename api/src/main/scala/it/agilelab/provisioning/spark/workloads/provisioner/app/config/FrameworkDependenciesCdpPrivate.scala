package it.agilelab.provisioning.spark.workloads.provisioner.app.config

import cats.effect.IO
import it.agilelab.provisioning.api.generated.Handler
import it.agilelab.provisioning.commons.principalsmapping.CdpIamPrincipals
import it.agilelab.provisioning.mesh.self.service.api.controller.ProvisionerController
import it.agilelab.provisioning.spark.workload.core.SparkCdpPrivate
import it.agilelab.provisioning.spark.workload.core.models.DpCdp
import it.agilelab.provisioning.spark.workloads.provisioner.app.api.SpecificProvisionerHandlerCdpPrivate

final class FrameworkDependenciesCdpPrivate(
  val provisionerController: ProvisionerController[DpCdp, SparkCdpPrivate, CdpIamPrincipals]
) extends FrameworkDependencies[SparkCdpPrivate] {
  override protected def createProvisionerHandler(
    provisionerController: ProvisionerController[DpCdp, SparkCdpPrivate, CdpIamPrincipals]
  ): Handler[IO] =
    new SpecificProvisionerHandlerCdpPrivate(provisionerController)
}

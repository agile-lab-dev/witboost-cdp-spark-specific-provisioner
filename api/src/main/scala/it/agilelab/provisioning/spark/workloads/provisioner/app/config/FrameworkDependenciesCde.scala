package it.agilelab.provisioning.spark.workloads.provisioner.app.config

import cats.effect.IO
import it.agilelab.provisioning.api.generated.Handler
import it.agilelab.provisioning.commons.principalsmapping.CdpIamPrincipals
import it.agilelab.provisioning.mesh.self.service.api.controller.ProvisionerController
import it.agilelab.provisioning.spark.workloads.core.SparkCde
import it.agilelab.provisioning.spark.workloads.core.models.DpCdp
import it.agilelab.provisioning.spark.workloads.provisioner.app.api.SpecificProvisionerHandlerCde

final class FrameworkDependenciesCde(
  val provisionerController: ProvisionerController[DpCdp, SparkCde, CdpIamPrincipals]
) extends FrameworkDependencies[SparkCde] {
  override protected def createProvisionerHandler(
    provisionerController: ProvisionerController[DpCdp, SparkCde, CdpIamPrincipals]
  ): Handler[IO] =
    new SpecificProvisionerHandlerCde(provisionerController)
}

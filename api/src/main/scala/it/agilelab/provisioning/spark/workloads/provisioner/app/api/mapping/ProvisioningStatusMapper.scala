package it.agilelab.provisioning.spark.workloads.provisioner.app.api.mapping

import it.agilelab.provisioning.api.generated.definitions.ProvisioningStatus
import it.agilelab.provisioning.mesh.self.service.api.model.ApiResponse
import it.agilelab.provisioning.mesh.self.service.api.model.ApiResponse.Status

object ProvisioningStatusMapper {

  def from(result: ApiResponse.ProvisioningStatus): ProvisioningStatus =
    result.status match {
      case Status.COMPLETED =>
        ProvisioningStatus(ProvisioningStatus.Status.Completed, result.result.getOrElse(""))
      case Status.FAILED    => ProvisioningStatus(ProvisioningStatus.Status.Failed, "")
      case Status.RUNNING   => ProvisioningStatus(ProvisioningStatus.Status.Failed, "")
    }

}

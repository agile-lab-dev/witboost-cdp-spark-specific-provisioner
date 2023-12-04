package it.agilelab.provisioning.spark.workloads.provisioner.service.gateway

import it.agilelab.provisioning.commons.audit.Audit
import it.agilelab.provisioning.mesh.self.service.core.gateway.{ ComponentGateway, ComponentGatewayError }
import it.agilelab.provisioning.mesh.self.service.core.model.ProvisionCommand
import io.circe.Json
import it.agilelab.provisioning.spark.workload.core.models.DpCdp
import it.agilelab.provisioning.spark.workload.core.{ SparkCde, SparkWorkloadResponse }

class CdeSparkWorkloadGatewayWithAudit(
  componentGateway: ComponentGateway[DpCdp, SparkCde, SparkWorkloadResponse],
  audit: Audit
) extends ComponentGateway[DpCdp, SparkCde, SparkWorkloadResponse] {
  override def create(
    request: ProvisionCommand[DpCdp, SparkCde]
  ): Either[ComponentGatewayError, SparkWorkloadResponse] = {
    audit.info(s"Executing Create($request)")
    val result = componentGateway.create(request)
    result match {
      case Right(_) => audit.info("SparkWorkload successfully created.")
      case Left(l)  => audit.error(s"SparkWorkload creation failed. Details: ${l.error}")
    }
    result
  }

  override def destroy(
    request: ProvisionCommand[DpCdp, SparkCde]
  ): Either[ComponentGatewayError, SparkWorkloadResponse] = {
    audit.info(s"Executing Destroy($request)")
    val result = componentGateway.destroy(request)
    result match {
      case Right(_) => audit.info("SparkWorkload successfully destroyed.")
      case Left(l)  => audit.error(s"SparkWorkload destroy failed. Details: ${l.error}")
    }
    result
  }
}

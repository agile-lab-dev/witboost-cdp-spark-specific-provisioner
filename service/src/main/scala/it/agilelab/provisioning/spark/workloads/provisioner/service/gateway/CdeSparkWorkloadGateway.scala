package it.agilelab.provisioning.spark.workloads.provisioner.service.gateway

import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.base.File.{ apply => _ }
import it.agilelab.provisioning.mesh.self.service.api.model.Component.Workload
import it.agilelab.provisioning.mesh.self.service.api.model.ProvisionRequest
import it.agilelab.provisioning.mesh.self.service.core.gateway.{ ComponentGatewayError, PermissionlessComponentGateway }
import it.agilelab.provisioning.mesh.self.service.core.model.ProvisionCommand
import it.agilelab.provisioning.spark.workloads.core.{ SparkCde, SparkWorkloadResponse }
import it.agilelab.provisioning.spark.workloads.core.models.DpCdp
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.mapper.{ SparkCdeWorkloadMapper }
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.workload.SparkCdeWorkloadGateway

class CdeSparkWorkloadGateway(
  sparkCdeWorkloadMapper: SparkCdeWorkloadMapper,
  sparkCdeWorkloadGateway: SparkCdeWorkloadGateway
) extends PermissionlessComponentGateway[DpCdp, SparkCde, SparkWorkloadResponse] {

  override def create(
    a: ProvisionCommand[DpCdp, SparkCde]
  ): Either[ComponentGatewayError, SparkWorkloadResponse] =
    a match {
      case ProvisionCommand(_, ProvisionRequest(dp, workload: Option[Workload[SparkCde]])) =>
        val result = workload match {
          case Some(w) =>
            for {
              sparkCdeWorkload <- sparkCdeWorkloadMapper.map(dp, w)
              res              <- sparkCdeWorkloadGateway.deployJob(sparkCdeWorkload)
            } yield res
          case None    =>
            Left(ComponentGatewayError(s"Unable to create resource, bad incoming request ${a.toString}"))
        }
        result

      case _ =>
        Left(ComponentGatewayError(s"Unable to create resource, bad incoming request ${a.toString}"))
    }

  override def destroy(a: ProvisionCommand[DpCdp, SparkCde]): Either[ComponentGatewayError, SparkWorkloadResponse] =
    a match {
      case ProvisionCommand(_, ProvisionRequest(dp, workload: Option[Workload[SparkCde]])) if workload.isDefined =>
        val result = workload match {
          case Some(w) =>
            for {
              sparkCdeWorkload <- sparkCdeWorkloadMapper.map(dp, w)
              res              <- sparkCdeWorkloadGateway.undeployJob(sparkCdeWorkload)
            } yield res
          case None    =>
            Left(ComponentGatewayError(s"Unable to destroy resource, bad incoming request ${a.toString}"))
        }
        result

      case _ =>
        Left(ComponentGatewayError(s"Unable to destroy resource, bad incoming request ${a.toString}"))
    }
}

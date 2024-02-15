package it.agilelab.provisioning.spark.workloads.provisioner.service.gateway

import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.base.File.{ apply => _ }
import it.agilelab.provisioning.mesh.self.service.api.model.Component.Workload
import it.agilelab.provisioning.mesh.self.service.api.model.ProvisionRequest
import it.agilelab.provisioning.mesh.self.service.core.gateway.{ ComponentGatewayError, PermissionlessComponentGateway }
import it.agilelab.provisioning.mesh.self.service.core.model.ProvisionCommand
import it.agilelab.provisioning.spark.workload.core.models.DpCdp
import it.agilelab.provisioning.spark.workload.core.{ SparkCdpPrivate, SparkWorkloadResponse }
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.mapper.SparkCdpPrivateWorkloadMapper
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.workload.SparkCdpPrivateWorkloadGateway

class CdpPrivateSparkWorkloadGateway(
  sparkCdpPrivateWorkloadMapper: SparkCdpPrivateWorkloadMapper,
  sparkCdpPrivateWorkloadGateway: SparkCdpPrivateWorkloadGateway
) extends PermissionlessComponentGateway[DpCdp, SparkCdpPrivate, SparkWorkloadResponse] {

  override def create(
    a: ProvisionCommand[DpCdp, SparkCdpPrivate]
  ): Either[ComponentGatewayError, SparkWorkloadResponse] =
    a match {
      case ProvisionCommand(_, ProvisionRequest(dp, workload: Option[Workload[SparkCdpPrivate]])) =>
        val result = workload match {
          case Some(w) =>
            for {
              sparkCdpPrivateWorkload <- sparkCdpPrivateWorkloadMapper.map(dp, w)
              res                     <- sparkCdpPrivateWorkloadGateway.deployJob(sparkCdpPrivateWorkload)
            } yield res
          case None    =>
            Left(ComponentGatewayError(s"Unable to create resource, bad incoming request ${a.toString}"))
        }
        result

      case _ =>
        Left(ComponentGatewayError(s"Unable to create resource, bad incoming request ${a.toString}"))
    }

  override def destroy(
    a: ProvisionCommand[DpCdp, SparkCdpPrivate]
  ): Either[ComponentGatewayError, SparkWorkloadResponse] =
    a match {
      case ProvisionCommand(_, ProvisionRequest(dp, workload: Option[Workload[SparkCdpPrivate]]))
          if workload.isDefined =>
        val result = workload match {
          case Some(w) =>
            for {
              sparkCdpPrivateWorkload <- sparkCdpPrivateWorkloadMapper.map(dp, w)
              res                     <- sparkCdpPrivateWorkloadGateway.undeployJob(sparkCdpPrivateWorkload)

            } yield res
          case None    =>
            Left(ComponentGatewayError(s"Unable to destroy resource, bad incoming request ${a.toString}"))
        }
        result

      case _ =>
        Left(ComponentGatewayError(s"Unable to destroy resource, bad incoming request ${a.toString}"))
    }
}

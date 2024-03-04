package it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.workload

import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.base.Job
import it.agilelab.provisioning.mesh.self.service.core.gateway.ComponentGatewayError
import it.agilelab.provisioning.spark.workloads.core.SparkWorkloadResponse

trait SparkWorkloadGateway[WORKLOAD] {
  def deployJob(sparkWorkload: WORKLOAD): Either[ComponentGatewayError, SparkWorkloadResponse]
  def undeployJob(sparkWorkload: WORKLOAD): Either[ComponentGatewayError, SparkWorkloadResponse]
}

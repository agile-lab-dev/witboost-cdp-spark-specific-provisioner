package it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.workload

import cats.implicits._
import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.request._
import it.agilelab.provisioning.mesh.self.service.core.gateway.ComponentGatewayError
import it.agilelab.provisioning.spark.workloads.core.SparkWorkloadResponse
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.clients.CdeClientFactory

class SparkCdeWorkloadGateway(cdeClientFactory: CdeClientFactory) extends SparkWorkloadGateway[SparkCdeWorkload] {
  private val ERROR: String         = "Job was properly created. but something goes wrong while trying to retrieve it"
  private val ERROR_DESTROY: String = "Job was properly destroyed but get job operation still return it"

  def deployJob(
    sparkCdeWorkload: SparkCdeWorkload
  ): Either[ComponentGatewayError, SparkWorkloadResponse] =
    for {
      cdeClient <- cdeClientFactory
                     .create(sparkCdeWorkload.domain, sparkCdeWorkload.dataProduct)
                     .leftMap(error => ComponentGatewayError(show"$error"))
      _         <- sparkCdeWorkload.resources
                     .map(r => cdeClient.safeCreateResource(CdeRequest(sparkCdeWorkload.service, sparkCdeWorkload.vc, r)))
                     .sequence
                     .leftMap(error => ComponentGatewayError(show"$error"))
      _         <- sparkCdeWorkload.artifacts
                     .map(r => cdeClient.uploadFile(CdeRequest(sparkCdeWorkload.service, sparkCdeWorkload.vc, r)))
                     .sequence
                     .leftMap(error => ComponentGatewayError(show"$error"))
      _         <- cdeClient
                     .upsertJob(CdeRequest(sparkCdeWorkload.service, sparkCdeWorkload.vc, UpsertJobReq(sparkCdeWorkload.job)))
                     .leftMap(error => ComponentGatewayError(show"$error"))
      getJobReq  = CdeRequest(sparkCdeWorkload.service, sparkCdeWorkload.vc, GetJobReq(sparkCdeWorkload.job.name))
      job       <- cdeClient
                     .getJob(getJobReq)
                     .leftMap(error => ComponentGatewayError(show"$error"))
      res       <- job.jobDetails.map(d => SparkWorkloadResponse(Some(d), None)).toRight(ComponentGatewayError(ERROR))
    } yield res

  def undeployJob(
    sparkCdeWorkload: SparkCdeWorkload
  ): Either[ComponentGatewayError, SparkWorkloadResponse] =
    for {
      cdeClient <- cdeClientFactory
                     .create(sparkCdeWorkload.domain, sparkCdeWorkload.dataProduct)
                     .leftMap(error => ComponentGatewayError(show"$error"))
      _         <-
        cdeClient
          .safeDeleteJob(
            CdeRequest(sparkCdeWorkload.service, sparkCdeWorkload.vc, DeleteJobReq(sparkCdeWorkload.job.name))
          )
          .leftMap(error => ComponentGatewayError(show"$error"))
      _         <- sparkCdeWorkload.resources
                     .map(r =>
                       cdeClient.safeDeleteResource(
                         CdeRequest(sparkCdeWorkload.service, sparkCdeWorkload.vc, DeleteResourceReq(r.name))
                       )
                     )
                     .sequence
                     .leftMap(error => ComponentGatewayError(show"$error"))
      getJobReq  = CdeRequest(sparkCdeWorkload.service, sparkCdeWorkload.vc, GetJobReq(sparkCdeWorkload.job.name))
      job       <- cdeClient.getJob(getJobReq).leftMap(error => ComponentGatewayError(show"$error"))
      _         <- job.jobDetails.fold[Either[ComponentGatewayError, Unit]](Right())(_ =>
                     Left(ComponentGatewayError(ERROR_DESTROY))
                   )
      res        = SparkWorkloadResponse(None, Some(sparkCdeWorkload.job))
    } yield res

}

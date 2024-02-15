package it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.mapper

import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.base.Job.spark
import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.base.Schedule.enable
import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.base.Schedule
import it.agilelab.provisioning.mesh.self.service.api.model.Component.Workload
import it.agilelab.provisioning.mesh.self.service.api.model.DataProduct
import it.agilelab.provisioning.mesh.self.service.core.gateway.ComponentGatewayError
import it.agilelab.provisioning.spark.workload.core.{ JobScheduler, SparkCdpPrivate }
import it.agilelab.provisioning.spark.workload.core.models.DpCdp
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.workload.SparkCdpPrivateWorkload
import it.agilelab.provisioning.spark.workload.core.SparkCdpPrivate._

class SparkCdpPrivateWorkloadMapper() extends SparkWorkloadMapper {
  def map(
    dataProduct: DataProduct[DpCdp],
    workload: Workload[SparkCdpPrivate]
  ): Either[ComponentGatewayError, SparkCdpPrivateWorkload] =
    for {
      mainResource <- Right(defineResourceName("mainres", workload.specific.jobName))
      schedule     <- Right(
                        workload.specific.jobConfig.flatMap(c =>
                          c.schedule.map(w => enable(user = "", w.cronExpression, w.startDate, w.endDate))
                        )
                      )

      sparkWorkload <- createSparkCdpPrivateWorkloadJob(
                         dataProduct,
                         workload,
                         mainResource,
                         schedule
                       ).left.map(error => ComponentGatewayError(s"Error creating SparkCdpPrivateWorkload: $error"))
    } yield sparkWorkload

  private def createSparkCdpPrivateWorkloadJob(
    dataProduct: DataProduct[DpCdp],
    workload: Workload[SparkCdpPrivate],
    mainResource: String,
    schedule: Option[Schedule]
  ): Either[String, SparkCdpPrivateWorkload] =
    workload.specific match {
      case SparkCdpPrivateJob(jn, appFile, className, jobConfig) =>
        val dataProductWorkload = SparkCdpPrivateWorkload(
          dataProduct.domain,
          dataProduct.name,
          spark(
            jn,
            mainResource,
            appFile,
            className,
            jobConfig.flatMap(_.dependencies).map(d => d.map(destArtifactPath)),
            jobConfig.flatMap(_.args),
            jobConfig.flatMap(_.driverCores),
            jobConfig.flatMap(_.driverMemory),
            jobConfig.flatMap(_.executorCores),
            jobConfig.flatMap(_.executorMemory),
            jobConfig.flatMap(_.numExecutors),
            schedule = schedule,
            logLevel = jobConfig.flatMap(_.logLevel)
          )
        )
        Right(dataProductWorkload)
      case _                                                     =>
        Left("Unsupported workload type: not a SparkCdpPrivateJob")
    }
}

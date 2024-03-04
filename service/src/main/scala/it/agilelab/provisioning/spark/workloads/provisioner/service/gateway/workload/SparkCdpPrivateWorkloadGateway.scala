package it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.workload

import cats.implicits._
import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.base.{ Job, JobDetails }
import it.agilelab.provisioning.mesh.self.service.core.gateway.ComponentGatewayError
import it.agilelab.provisioning.spark.workloads.core.SparkWorkloadResponse
import it.agilelab.provisioning.spark.workloads.provisioner.quartz.SchedulingService

import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.TimeZone

class SparkCdpPrivateWorkloadGateway(schedulerService: SchedulingService)
    extends SparkWorkloadGateway[SparkCdpPrivateWorkload] {

  def deployJob(
    sparkCdpPrivateWorkload: SparkCdpPrivateWorkload
  ): Either[ComponentGatewayError, SparkWorkloadResponse] =
    schedulerService.scheduleJob(
      sparkCdpPrivateWorkload.job,
      sparkCdpPrivateWorkload.dataProduct,
      sparkCdpPrivateWorkload.queue
    ) match {
      case Left(error)       =>
        Left(ComponentGatewayError(error.message))
      case Right(date: Date) =>
        Right(SparkWorkloadResponse.create(mapJobToJobDetails(sparkCdpPrivateWorkload.job, date)))
    }

  def undeployJob(
    sparkCdpPrivateWorkload: SparkCdpPrivateWorkload
  ): Either[ComponentGatewayError, SparkWorkloadResponse] =
    for {
      _ <- schedulerService
             .unscheduleJob(sparkCdpPrivateWorkload.job.name, sparkCdpPrivateWorkload.dataProduct)
             .leftMap(error => ComponentGatewayError(error.message))

      res = SparkWorkloadResponse(None, Some(sparkCdpPrivateWorkload.job))
    } yield res

  def mapJobToJobDetails(job: Job, date: Date): JobDetails =
    JobDetails(
      name = job.name,
      `type` = job.`type`,
      created = date.toString,
      modified = date.toString,
      lastUsed = date.toString,
      mounts = job.mounts,
      retentionPolicy = job.retentionPolicy,
      spark = job.spark,
      schedule = job.schedule
    )

}

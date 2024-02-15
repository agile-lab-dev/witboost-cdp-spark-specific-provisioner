package it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.workload

import cats.implicits._
import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.base.{ Job, JobDetails }
import it.agilelab.provisioning.mesh.self.service.core.gateway.ComponentGatewayError
import it.agilelab.provisioning.spark.workload.core.SparkWorkloadResponse
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
  ): Either[ComponentGatewayError, SparkWorkloadResponse] = {

    val jobName = sparkCdpPrivateWorkload.job.name
    val jarPath = sparkCdpPrivateWorkload.job.spark.fold("SparkJobNotFound")(_.file)

    val currentDateTime = ZonedDateTime.now
    val formatter       = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX")

    val sparkClassName          =
      sparkCdpPrivateWorkload.job.spark.fold("SparkJobNotFound")(_.className.getOrElse("ClassNameNotFound"))
    val cronExp: Option[String] = sparkCdpPrivateWorkload.job.schedule.flatMap(_.cronExpression)
    val startDateString         =
      sparkCdpPrivateWorkload.job.schedule.fold("SparkJobNotFound")(
        _.start.getOrElse(currentDateTime.format(formatter))
      )
    val endDateString           =
      sparkCdpPrivateWorkload.job.schedule.fold("SparkJobNotFound")(
        _.end.getOrElse(currentDateTime.plusYears(20).format(formatter))
      )

    val dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
    val startDate  = dateFormat.parse(startDateString)
    val endDate    = dateFormat.parse(endDateString)

    schedulerService.scheduleJob(
      jobName,
      sparkCdpPrivateWorkload.dataProduct,
      jarPath,
      sparkClassName,
      cronExp,
      startDate,
      endDate
    ) match {
      case Left(error)       =>
        Left(ComponentGatewayError(error.message))
      case Right(date: Date) =>
        Right(SparkWorkloadResponse.create(mapJobToJobDetails(sparkCdpPrivateWorkload.job, date)))
    }
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

package it.agilelab.provisioning.spark.workloads.provisioner.quartz

import java.util.Date

trait SchedulingService {
  def scheduleJob(
    jobName: String,
    jobGroup: String,
    jarPath: String,
    sparkClassName: String,
    cronExp: Option[String],
    startDate: Date,
    endDate: Date
  ): Either[SchedulerError, Date]
  def unscheduleJob(jobName: String, jobGroup: String): Either[SchedulerError, Boolean]
}

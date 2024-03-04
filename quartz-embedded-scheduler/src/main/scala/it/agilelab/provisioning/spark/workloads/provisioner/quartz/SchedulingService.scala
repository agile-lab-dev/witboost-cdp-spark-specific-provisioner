package it.agilelab.provisioning.spark.workloads.provisioner.quartz

import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.base.Job

import java.util.Date

trait SchedulingService {
  def scheduleJob(
    job: Job,
    jobGroup: String,
    queue: String
  ): Either[SchedulerError, Date]
  def unscheduleJob(jobName: String, jobGroup: String): Either[SchedulerError, Boolean]
}

package it.agilelab.provisioning.spark.workload.core

import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.base.{ Job, JobDetails }

final case class SparkWorkloadResponse(createdJob: Option[JobDetails], destroyedJob: Option[Job])

object SparkWorkloadResponse {
  def create(job: JobDetails): SparkWorkloadResponse =
    SparkWorkloadResponse(Some(job), None)

  def destroy(job: Job): SparkWorkloadResponse =
    SparkWorkloadResponse(None, Some(job))
}

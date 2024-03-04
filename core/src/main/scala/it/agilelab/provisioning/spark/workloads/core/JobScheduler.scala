package it.agilelab.provisioning.spark.workloads.core

final case class JobScheduler(
  cronExpression: String,
  startDate: String,
  endDate: String
)

object JobScheduler {
  def create(cronExpression: String, startDate: String, endDate: String): JobScheduler =
    JobScheduler(cronExpression, startDate, endDate)
}

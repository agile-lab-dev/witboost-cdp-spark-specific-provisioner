package it.agilelab.provisioning.spark.workloads.core

final case class JobConfig(
  args: Option[Seq[String]],
  dependencies: Option[Seq[String]],
  driverCores: Option[Int],
  driverMemory: Option[String],
  executorCores: Option[Int],
  executorMemory: Option[String],
  numExecutors: Option[Int],
  logLevel: Option[String],
  conf: Option[Map[String, String]],
  schedule: Option[JobScheduler]
)

package it.agilelab.provisioning.spark.workloads.provisioner.quartz.embedded.scheduler

import org.quartz.{ Job, JobExecutionContext }

class TestJob extends Job {
  override def execute(context: JobExecutionContext): Unit =
    println("Executing it.agilelab.provisioning.spark.workloads.provisioner.quartz.embedded.scheduler.TestJob...")
}

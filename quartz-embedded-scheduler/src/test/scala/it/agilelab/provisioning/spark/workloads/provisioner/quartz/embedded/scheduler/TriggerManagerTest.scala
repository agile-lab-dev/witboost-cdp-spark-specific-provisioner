package it.agilelab.provisioning.spark.workloads.provisioner.quartz.embedded.scheduler

import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.base.{ Job, Schedule, SparkJob }
import it.agilelab.provisioning.spark.workloads.core.SparkCdpPrivate.SparkCdpPrivateJob
import it.agilelab.provisioning.spark.workloads.provisioner.quartz.{ SchedulerError, TriggerManager }
import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite
import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.base.Mount

import java.util.Date

class TriggerManagerTest extends AnyFunSuite with MockFactory {

  test("TriggerManager should create a trigger successfully") {
    val triggerManager = new TriggerManager()

    val myJob = Job(
      name = "testJob",
      `type` = "spark",
      mounts = List(Mount("mainres-6f3dcb8f8768f7e34f0a50cc08de8ff5")),
      retentionPolicy = "keep_indefinitely",
      spark = Some(
        SparkJob(
          file = "hdfs://prova.jar",
          driverCores = 1,
          driverMemory = "1g",
          executorCores = 1,
          executorMemory = "1g",
          logLevel = Some("INFO"),
          numExecutors = Some(1),
          className = Some("className"),
          args = Some(List("a", "b", "c")),
          conf = Some(Map("dex.safariEnabled" -> "true")),
          jars = Some(List()),
          proxyUser = None,
          pythonEnvResourceName = None,
          pyFiles = None
        )
      ),
      airflow = None,
      schedule = Some(
        Schedule(
          enabled = true,
          user = Some(""),
          paused = None,
          catchup = None,
          dependsOnPast = None,
          pausedUponCreation = None,
          start = Some("2024-02-19T14:22:00Z"),
          end = Some("2024-03-08T13:36:00Z"),
          cronExpression = Some("00 * * * * ?"),
          nextExecution = None
        )
      )
    )

    val jobName   = "testJob"
    val groupName = "testGroup"
    val interval  = "0/10 * * * * ?"
    val startDate = new Date()
    val endDate   = new Date(System.currentTimeMillis() + 3600 * 1000)

    val result = triggerManager.createTrigger(myJob, groupName)

    result match {
      case Left(error)    =>
        fail(s"Failed to create trigger: $error")
      case Right(trigger) =>
        assert(trigger.getKey.getName === s"${jobName}Trigger")
        assert(trigger.getKey.getGroup === groupName)
    }
  }

  test("TriggerManager should handle errors when creating a trigger") {
    val triggerManager = new TriggerManager()

    val myJob = Job(
      name = "",
      `type` = "spark",
      mounts = List(Mount("mainres-6f3dcb8f8768f7e34f0a50cc08de8ff5")),
      retentionPolicy = "keep_indefinitely",
      spark = Some(
        SparkJob(
          file = "hdfs://prova.jar",
          driverCores = 1,
          driverMemory = "1g",
          executorCores = 1,
          executorMemory = "1g",
          logLevel = Some("INFO"),
          numExecutors = Some(1),
          className = Some("className"),
          args = Some(List("a", "b", "c")),
          conf = Some(Map("dex.safariEnabled" -> "true")),
          jars = Some(List()),
          proxyUser = None,
          pythonEnvResourceName = None,
          pyFiles = None
        )
      ),
      airflow = None,
      schedule = Some(
        Schedule(
          enabled = true,
          user = Some(""),
          paused = None,
          catchup = None,
          dependsOnPast = None,
          pausedUponCreation = None,
          start = Some("2024-02-19T14:22:00Z"),
          end = Some("2024-03-08T13:36:00Z"),
          cronExpression = Some("00 * * * * ?"),
          nextExecution = None
        )
      )
    )

    val jobName   = "testJob"
    val groupName = "testGroup"
    val interval  = "0/10 * * * * ?"
    val startDate = new Date()
    val endDate   = new Date(System.currentTimeMillis() + 3600 * 1000)

    // Forcing an error by providing an invalid job name
    val result = triggerManager.createTrigger(myJob, groupName)

    result match {
      case Left(error) =>
        assert(error.isInstanceOf[SchedulerError])
      case Right(_)    =>
        fail("Expected an error but trigger creation was successful")
    }
  }
}

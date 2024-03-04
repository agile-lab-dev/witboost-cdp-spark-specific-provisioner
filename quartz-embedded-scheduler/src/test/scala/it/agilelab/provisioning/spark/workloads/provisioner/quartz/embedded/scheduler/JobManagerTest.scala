package it.agilelab.provisioning.spark.workloads.provisioner.quartz.embedded.scheduler

import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.base.{ Job => SparkJobCommons }
import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.base.{ Mount, Schedule, SparkJob }
import it.agilelab.provisioning.spark.workloads.provisioner.quartz.{ JobManager, SchedulerError }
import org.quartz._
import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite

import java.util.Collections
import scala.jdk.CollectionConverters._

class JobManagerTest extends AnyFunSuite with MockFactory {

  val scheduler: Scheduler = mock[Scheduler]
  val jobManager           = new JobManager(scheduler)

  test("create a job successfully") {

    val myJob = SparkJobCommons(
      name = "testJob",
      `type` = "spark",
      mounts = List(Mount("mainres-6f3dcb8f8768f7e34f0a50cc08de8ff5")),
      retentionPolicy = "keep_indefinitely",
      spark = Some(
        SparkJob(
          file = "path/to/jar",
          driverCores = 1,
          driverMemory = "1g",
          executorCores = 1,
          executorMemory = "1g",
          logLevel = Some("INFO"),
          numExecutors = Some(1),
          className = Some("com.example.Main"),
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

    val jobName                   = "testJob"
    val jobGroup                  = "testGroup"
    val jobClass: Class[_ <: Job] = classOf[TestJob]
    val jarPath                   = "path/to/jar"
    val sparkMainClassName        = "com.example.Main"
    val queue                     = "default"

    val result = jobManager.createJob(myJob, jobGroup, queue, jobClass)
    assert(result.isRight)
    result match {
      case Right(jobDetail) =>
        assert(jobDetail.getKey.getName == jobName)
        assert(jobDetail.getKey.getGroup == jobGroup)
        assert(jobDetail.getJobDataMap.get("jarPath") == jarPath)
        assert(jobDetail.getJobDataMap.get("className") == sparkMainClassName)
      case Left(error)      =>
        fail(s"Failed to create job: $error")
    }
  }

  test("delete an existing job successfully") {
    val jobName  = "testJob"
    val jobGroup = "testGroup"

    (scheduler.deleteJob _).expects(*).returning(true)

    val result = jobManager.deleteJob(jobName, jobGroup)
    result match {
      case Right(success) =>
        assert(success)
      case Left(error)    =>
        fail(s"Failed to delete job: $error")
    }
  }

  test("handle error during job deletion") {
    val jobName  = "testJob"
    val jobGroup = "testGroup"

    (scheduler.deleteJob _).expects(*).throws(new SchedulerException("Test Exception"))

    val result = jobManager.deleteJob(jobName, jobGroup)
    result match {
      case Left(error) =>
        error match {
          case jobSchedulerError: SchedulerError =>
            assert(jobSchedulerError.message == "SchedulerException while deleting job: Test Exception")
          case _                                 =>
            fail("Expected JobSchedulerError")
        }
      case Right(_)    =>
        fail("Expected Left with JobSchedulerError")
    }
  }

  test("return false for a non-existing job") {
    val jobName  = "nonExistingJob"
    val jobGroup = "nonExistingGroup"

    (scheduler.getJobKeys _).expects(*).returning(Collections.emptySet())

    val result = jobManager.jobExists(jobName, jobGroup)

    assert(!result)
  }

  test("return true for an existing job") {
    val jobName  = "existingJob"
    val jobGroup = "existingGroup"

    val jobKey  = new JobKey(jobName, jobGroup)
    val jobKeys = Set(jobKey).asJava

    (scheduler.getJobKeys _).expects(*).returning(jobKeys)

    val exists = jobManager.jobExists(jobName, jobGroup)

    assert(exists)
  }
}

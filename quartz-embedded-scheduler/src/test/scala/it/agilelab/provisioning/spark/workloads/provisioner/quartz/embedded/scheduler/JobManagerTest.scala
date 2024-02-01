package it.agilelab.provisioning.spark.workloads.provisioner.quartz.embedded.scheduler

import org.quartz._
import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite
import quartz.{ JobManager, JobSchedulerError }

import java.util.Collections
import scala.jdk.CollectionConverters._

class JobManagerTest extends AnyFunSuite with MockFactory {

  val scheduler: Scheduler = mock[Scheduler]
  val jobManager           = new JobManager(scheduler)

  test("create a job successfully") {
    val jobName                   = "testJob"
    val jobGroup                  = "testGroup"
    val jobClass: Class[_ <: Job] = classOf[TestJob]
    val jarPath                   = "path/to/jar"
    val sparkMainClassName        = "com.example.Main"

    val result = jobManager.createJob(jobName, jobGroup, jobClass, jarPath, sparkMainClassName)
    assert(result.isRight)
    result match {
      case Right(jobDetail) =>
        assert(jobDetail.getKey.getName == jobName)
        assert(jobDetail.getKey.getGroup == jobGroup)
        assert(jobDetail.getJobDataMap.getString("jarPath") == jarPath)
        assert(jobDetail.getJobDataMap.getString("className") == sparkMainClassName)
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
          case jobSchedulerError: JobSchedulerError =>
            assert(jobSchedulerError.message == "SchedulerException while deleting job: Test Exception")
          case _                                    =>
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

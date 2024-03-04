package it.agilelab.provisioning.spark.workloads.provisioner.quartz

import com.typesafe.scalalogging.Logger
import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.base.SparkJob.{
  DEFAULT_DRIVER_CORES,
  DEFAULT_DRIVER_MEMORY,
  DEFAULT_EXECUTOR_CORES,
  DEFAULT_EXECUTOR_MEMORY,
  DEFAULT_NUM_EXECUTOR
}
import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.base.{ Job => SparkJobCommons }
import org.quartz.JobKey.jobKey
import org.quartz.impl.matchers.GroupMatcher
import org.quartz.{ Job, JobBuilder, JobDetail, JobKey, Scheduler, SchedulerException }

import scala.jdk.CollectionConverters._

class JobManager(scheduler: Scheduler) {

  private val logger                   = Logger(getClass.getName)
  private val JAR_PATH_NOT_FOUND       = "Jar path not found"
  private val CLASS_NAME_NOT_FOUND     = "Class name not found"
  def createJob(
    job: SparkJobCommons,
    jobGroup: String,
    queue: String,
    jobClass: Class[_ <: Job]
  ): Either[SchedulerError, JobDetail] =
    try {

      val jobName = job.name
      val jarPath = job.spark.map(_.file).getOrElse(JAR_PATH_NOT_FOUND)

      val spark = job.spark

      val jobArgs: Seq[String]   = spark.flatMap(_.args).getOrElse(Seq.empty)
      val sparkClassName         = spark.flatMap(_.className).getOrElse(CLASS_NAME_NOT_FOUND)
      val conf                   = spark.flatMap(_.conf).getOrElse(Map.empty)
      val driverMemory: String   = spark.map(_.driverMemory).getOrElse(DEFAULT_DRIVER_MEMORY)
      val driverCores: Int       = spark.map(_.driverCores).getOrElse(DEFAULT_DRIVER_CORES)
      val executorMemory: String = spark.map(_.executorMemory).getOrElse(DEFAULT_EXECUTOR_MEMORY)
      val executorCores: Int     = spark.map(_.executorCores).getOrElse(DEFAULT_EXECUTOR_CORES)
      val numExecutors: Int      = spark.flatMap(_.numExecutors).getOrElse(DEFAULT_NUM_EXECUTOR)

      val jobDetail = JobBuilder
        .newJob(jobClass)
        .withIdentity(jobName, jobGroup)
        .storeDurably(true)
        .build()

      val jobDataMap = jobDetail.getJobDataMap

      val _ = jobDataMap.put("className", sparkClassName)
      val _ = jobDataMap.put("jarPath", jarPath)
      val _ = jobDataMap.put("jobArgs", jobArgs)
      val _ = jobDataMap.put("driverMemory", driverMemory)
      val _ = jobDataMap.put("driverCores", driverCores)
      val _ = jobDataMap.put("executorMemory", executorMemory)
      val _ = jobDataMap.put("executorCores", executorCores)
      val _ = jobDataMap.put("numExecutors", numExecutors)
      val _ = jobDataMap.put("conf", conf)
      val _ = jobDataMap.put("name", jobName)
      val _ = jobDataMap.put("queue", queue)

      logger.info(s"Created job: $jobDetail")

      Right(jobDetail)
    } catch {
      case e: SchedulerException =>
        Left(SchedulerError(s"SchedulerException while creating job: ${e.getMessage}"))
      case _: Throwable          =>
        Left(SchedulerError("Unexpected error while creating job."))
    }

  def deleteJob(jobName: String, jobGroup: String): Either[SchedulerError, Boolean] =
    try {
      val res = scheduler.deleteJob(jobKey(jobName, jobGroup))

      logger.info(s"Deleted job: $res")

      Right(res)
    } catch {
      case e: SchedulerException =>
        Left(SchedulerError(s"SchedulerException while deleting job: ${e.getMessage}"))
      case _: Throwable          =>
        Left(SchedulerError("Unexpected error while deleting job."))
    }

  def jobExists(jobName: String, jobGroup: String): Boolean = {

    val jobKeys: List[JobKey] = scheduler.getJobKeys(GroupMatcher.anyJobGroup()).asScala.toList
    val jobKeyToSearch        = new JobKey(jobName, jobGroup)

    jobKeys.contains(jobKeyToSearch)

  }

}

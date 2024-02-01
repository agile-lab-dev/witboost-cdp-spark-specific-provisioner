package quartz

import org.quartz.JobKey.jobKey
import org.quartz.impl.matchers.GroupMatcher
import org.quartz._

import scala.jdk.CollectionConverters._

class JobManager(scheduler: Scheduler) {

  def createJob(
    jobName: String,
    jobGroup: String,
    jobClass: Class[_ <: Job],
    jarPath: String,
    sparkMainClassName: String
  ): Either[JobManagerError, JobDetail] =
    try {
      val jobDetail = JobBuilder
        .newJob(jobClass)
        .withIdentity(jobName, jobGroup)
        .build()

      jobDetail.getJobDataMap.put("jarPath", jarPath)
      jobDetail.getJobDataMap.put("className", sparkMainClassName)

      Right(jobDetail)
    } catch {
      case e: SchedulerException =>
        Left(JobSchedulerError(s"SchedulerException while creating job: ${e.getMessage}"))
      case _: Throwable          =>
        Left(JobUnexpectedError)
    }

  def deleteJob(jobName: String, jobGroup: String): Either[JobManagerError, Boolean] =
    try {
      val res = scheduler.deleteJob(jobKey(jobName, jobGroup))
      Right(res)
    } catch {
      case e: SchedulerException =>
        Left(JobSchedulerError(s"SchedulerException while deleting job: ${e.getMessage}"))
      case _                     =>
        Left(JobUnexpectedError)
    }

  def jobExists(jobName: String, jobGroup: String): Boolean = {

    val jobKeys: List[JobKey] = scheduler.getJobKeys(GroupMatcher.anyJobGroup()).asScala.toList
    val jobKeyToSearch        = new JobKey(jobName, jobGroup)

    jobKeys.contains(jobKeyToSearch)

  }

}

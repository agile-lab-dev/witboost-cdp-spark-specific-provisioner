package it.agilelab.provisioning.spark.workloads.provisioner.quartz

import com.typesafe.config.ConfigFactory
import it.agilelab.provisioning.spark.workloads.provisioner.quartz.config.ApplicationConfiguration
import org.quartz.{ Scheduler, SchedulerException }
import org.quartz.impl.StdSchedulerFactory

import java.util.Date
class SchedulingServiceWithQuartz(propertiesFile: Option[String]) extends SchedulingService {

  private val scheduler      = createScheduler(propertiesFile)
  private val jobManager     = new JobManager(scheduler)
  private val triggerManager = new TriggerManager()
  override def scheduleJob(
    jobName: String,
    jobGroup: String,
    jarPath: String,
    sparkClassName: String,
    cronExp: Option[String],
    startDate: Date,
    endDate: Date
  ): Either[SchedulerError, Date] = {

    val conf = ConfigFactory.load()

    val livyServerUrl: Option[String] = if (conf.hasPath("provisioner.livy-url")) {
      val livyUrl = ApplicationConfiguration.provisionerConfig.getString(ApplicationConfiguration.LIVY_URL)

      if (livyUrl.nonEmpty) Some(livyUrl) else None
    } else None

    livyServerUrl match {
      case Some(livyServerUrl) =>
        scheduler.getContext().put("livyServerUrl", livyServerUrl)

        if (jobManager.jobExists(jobName, jobGroup)) {
          jobManager.deleteJob(jobName, jobGroup) match {
            case Left(error)    =>
              Left(SchedulerError(s"Error overwriting the previous job. Deletion failed, errors: $error"))
            case Right(deleted) =>
              if (!deleted) Left(SchedulerError(s"Error overwriting the previous job. Deletion failed."))
              else createAndScheduleJob(jobName, jobGroup, jarPath, sparkClassName, cronExp, startDate, endDate)
          }
        } else {
          createAndScheduleJob(jobName, jobGroup, jarPath, sparkClassName, cronExp, startDate, endDate)
        }

      case None =>
        Left(SchedulerError("Livy URL not found in env vars"))
    }
  }

  private def createAndScheduleJob(
    jobName: String,
    jobGroup: String,
    jarPath: String,
    sparkClassName: String,
    cronExp: Option[String],
    startDate: Date,
    endDate: Date
  ): Either[SchedulerError, Date] = {

    val jobClass = classOf[LivyJob]

    try {
      val createJobResult     = jobManager.createJob(jobName, jobGroup, jobClass, jarPath, sparkClassName)
      val createTriggerResult = triggerManager.createTrigger(jobName, jobGroup, cronExp, startDate, endDate)

      (createJobResult, createTriggerResult) match {
        case (Right(jobDetail), Right(trigger))   => Right(scheduler.scheduleJob(jobDetail, trigger))
        case (Left(jobError), Left(triggerError)) =>
          Left(SchedulerError(s"Error creating job: $jobError, Error creating trigger: $triggerError"))
        case (Left(jobError), _)                  =>
          Left(SchedulerError(s"Error creating job: $jobError"))
        case (_, Left(triggerError))              =>
          Left(SchedulerError(s"Error creating trigger: $triggerError"))
        case _                                    =>
          Left(SchedulerError("Unknown error scheduling job"))
      }
    } catch {
      case e: SchedulerException => Left(SchedulerError(s"Error while scheduling job: $e"))
    }
  }

  override def unscheduleJob(jobName: String, jobGroup: String): Either[SchedulerError, Boolean] =
    jobManager.deleteJob(jobName, jobGroup)

  private def createScheduler(propertiesFile: Option[String]): Scheduler =
    propertiesFile match {
      case Some(prop) =>
        val newScheduler = new StdSchedulerFactory(prop).getScheduler()
        newScheduler
      case None       =>
        val newScheduler = new StdSchedulerFactory().getScheduler()
        newScheduler
    }
  def startScheduler(): Unit                                             =
    scheduler.start()

  def shutdownScheduler(): Unit =
    scheduler.shutdown(true)
}

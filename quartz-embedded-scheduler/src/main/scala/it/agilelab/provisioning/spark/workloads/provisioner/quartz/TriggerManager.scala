package it.agilelab.provisioning.spark.workloads.provisioner.quartz

import com.typesafe.scalalogging.Logger
import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.base.{ Job => SparkJobCommons }
import org.quartz.{ CronScheduleBuilder, SchedulerException, Trigger, TriggerBuilder }

import java.text.SimpleDateFormat
import java.time.{ ZoneId, ZonedDateTime }
import java.time.format.DateTimeFormatter
import java.util.TimeZone

class TriggerManager() {

  private val logger = Logger(getClass.getName)

  def createTrigger(
    job: SparkJobCommons,
    jobGroup: String
  ): Either[SchedulerError, Trigger] =
    try {

      val jobName = job.name

      val currentDateTime: ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC"))
      val formatter                      = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX")

      val cronExp: Option[String] = job.schedule.flatMap(_.cronExpression)
      val startDateString         = job.schedule.flatMap(_.start).getOrElse(currentDateTime.format(formatter))
      val endDateString           = job.schedule.flatMap(_.end).getOrElse(currentDateTime.plusYears(20).format(formatter))

      val dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
      dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
      val startDate  = dateFormat.parse(startDateString)
      val endDate    = dateFormat.parse(endDateString)

      val triggerBuilder = TriggerBuilder
        .newTrigger()
        .withIdentity(s"${jobName}Trigger", jobGroup)
        .startAt(startDate)
        .endAt(endDate)
        .forJob(jobName, jobGroup)

      val triggerWithSchedule = cronExp.map { cronExpr =>
        triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cronExpr)).build()
      }

      val result = triggerWithSchedule.getOrElse(triggerBuilder.build())

      logger.info(s"Created trigger: $result")

      Right(result)
    } catch {
      case e: SchedulerException =>
        Left(SchedulerError(s"SchedulerException while creating trigger: ${e.getMessage}"))
      case e: Throwable          =>
        Left(SchedulerError(s"Unexpected error while creating trigger. ${e.getMessage}"))
    }
}

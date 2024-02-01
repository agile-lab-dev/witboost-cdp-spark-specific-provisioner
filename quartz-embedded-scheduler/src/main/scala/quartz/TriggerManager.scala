package quartz

import org.quartz._

import java.util.Date

class TriggerManager() {
  def createTrigger(
    jobName: String,
    groupName: String,
    cronExpression: Option[String] = None,
    startDate: Date,
    endDate: Date
  ): Either[TriggerManagerError, Trigger] =
    try {

      val triggerBuilder = TriggerBuilder
        .newTrigger()
        .withIdentity(s"${jobName}Trigger", groupName)
        .startAt(startDate)
        .endAt(endDate)
        .forJob(jobName, groupName)

      val triggerWithSchedule = cronExpression.map { cronExpr =>
        triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cronExpr)).build()
      }

      Right(triggerWithSchedule.getOrElse(triggerBuilder.build()))
    } catch {
      case e: SchedulerException =>
        Left(TriggerSchedulerError(s"SchedulerException while creating trigger: ${e.getMessage}"))
      case _: Throwable          =>
        Left(TriggerUnexpectedError)
    }
}

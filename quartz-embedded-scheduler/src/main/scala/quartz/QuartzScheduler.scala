package quartz

import org.postgresql.util.PSQLException
import org.quartz.impl.StdSchedulerFactory
import org.slf4j.LoggerFactory

import java.time.LocalDate
import java.util.{ Calendar, Date }

//**********************
//This is an example class that is used until the
//real implementation of the provisioner to show a possible use of the classes in the package
//**********************
object QuartzScheduler {
  private val logger = LoggerFactory.getLogger(getClass)
  def main(args: Array[String]): Unit = {

    try {
      val scheduler = new StdSchedulerFactory("schedulerProp.properties").getScheduler()

      val jobManager     = new JobManager(scheduler)
      val triggerManager = new TriggerManager()

      val jobName  = "LivyJob"
      val jobGroup = "group1"
      val jarPath  = "jarPathHere"

      val livyServerUrl  = "http://localhost:8998"
      val sparkClassName = "resources.SparkPiExample.SparkPiExample"

      scheduler.getContext().put("livyServerUrl", livyServerUrl)

      jobManager.createJob(jobName, jobGroup, classOf[LivyJob], jarPath, sparkClassName) match {
        case Left(error) =>
          logger.error(s"Error creating job: $error")

        case Right(job) =>
          if (jobManager.jobExists(jobName, jobGroup)) {
            jobManager.deleteJob(jobName, jobGroup) match {
              case Left(error)    =>
                logger.error(s"Error deleting existing job: $error")
              case Right(deleted) =>
                if (deleted) logger.info("Existing job deleted successfully.")
                else logger.error("Error deleting job.")
            }
          }

          val today    = new Date()
          val calendar = Calendar.getInstance()
          calendar.setTime(today)
          calendar.add(Calendar.DAY_OF_MONTH, 1)
          val tomorrow = calendar.getTime()

          triggerManager.createTrigger(jobName, jobGroup, Some("0/10 * * * * ?"), today, tomorrow) match {
            case Left(error) =>
              logger.error(s"Error creating trigger: $error")

            case Right(trigger) =>
              scheduler.start()
              val scheduledDate = scheduler.scheduleJob(job, trigger)
              logger.info(s"Job scheduled for: $scheduledDate")
              Thread.sleep(60000)

              jobManager.deleteJob(jobName, jobGroup) match {
                case Left(error)    =>
                  logger.error(s"Error deleting job: $error")
                case Right(deleted) =>
                  if (deleted) logger.info("Job deleted successfully.")
                  else logger.error("Error deleting job.")
              }
              scheduler.shutdown(true)
          }
      }
    } catch {
      case e: PSQLException =>
        logger.error(s"Unable to connect to the database. Error:  $e")
      case e: Exception     =>
        logger.error(s"Unexpected error:  $e")
    }
  }
}

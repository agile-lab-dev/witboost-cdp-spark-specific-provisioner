package quartz

import org.quartz.{ Job, JobExecutionContext, JobExecutionException }
import org.slf4j.LoggerFactory
import scalaj.http.{ Http, HttpResponse }

import scala.util.{ Failure, Success, Try }

class LivyJob() extends Job {
  private val logger = LoggerFactory.getLogger(getClass)

  @throws[JobExecutionException]
  override def execute(context: JobExecutionContext): Unit =
    try {

      val livyServerUrl  = context.getScheduler.getContext.get("livyServerUrl")
      val livyRequestUrl = s"$livyServerUrl/batches"

      val data      = context.getJobDetail.getJobDataMap
      val jarPath   = data.getString("jarPath")
      val className = data.getString("className")

      val sparkJobParams = Map(
        "file"      -> jarPath,
        "className" -> className
      )

      val response: Try[HttpResponse[String]] = Try {
        Http(livyRequestUrl)
          .postData(s"""${sparkJobParams.map { case (k, v) => s""""$k":"$v"""" }.mkString("{", ",", "}")}""")
          .header("Content-Type", "application/json")
          .asString
      }

      response match {
        case Success(res) =>
          logger.info(s"HTTP Status: ${res.code}")
          logger.info(s"Response Body: ${res.body}")
        case Failure(ex)  =>
          logger.error(s"Failed to submit the Spark job: ${ex.getMessage}")
      }

    } catch {
      case e: Exception => logger.error(s"Unexpected exception: ${e.getMessage}")
    }

}

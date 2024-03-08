package it.agilelab.provisioning.spark.workloads.provisioner.quartz

import it.agilelab.provisioning.spark.workloads.core.context.cdpPrivate.httpclient.HttpClientFactory
import org.apache.http.entity.{ ContentType, StringEntity }
import org.quartz.{ Job, JobExecutionContext, JobExecutionException }
import org.slf4j.LoggerFactory
import play.api.libs.json.{ JsObject, JsString, Json }

import java.time.{ ZoneId, ZonedDateTime }
import java.time.format.DateTimeFormatter

class LivyJob() extends Job {

  private val logger = LoggerFactory.getLogger(getClass)

  @throws[JobExecutionException]
  override def execute(context: JobExecutionContext): Unit =
    sendLivyRequest(context)

  private def sendLivyRequest(context: JobExecutionContext): Unit = {

    val livyServerUrl  = context.getScheduler.getContext.get("livyServerUrl")
    val livyRequestUrl = s"$livyServerUrl/batches"

    val data              = context.getJobDetail.getJobDataMap
    val jarPath           = data.getString("jarPath")
    val className         = data.getString("className")
    val args: Seq[String] = data.get("jobArgs").asInstanceOf[Seq[String]]

    val driverMemory   = data.getString("driverMemory")
    val driverCores    = data.getInt("driverCores")
    val executorMemory = data.getString("executorMemory")
    val executorCores  = data.getInt("executorCores")
    val numExecutors   = data.getInt("numExecutors")
    val conf           = data.get("conf").asInstanceOf[Map[String, String]]
    val name           = data.getString("name")
    val queue          = data.getString("queue")

    val currentDateTime: ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC"))
    val formatter                      = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a")
    val dateTimeStr                    = currentDateTime.format(formatter)

    val baseSparkJobParams: JsObject = Json.obj(
      "file"           -> jarPath,
      "className"      -> className,
      "args"           -> Json.toJson(args),
      "driverMemory"   -> driverMemory,
      "driverCores"    -> driverCores,
      "executorMemory" -> executorMemory,
      "executorCores"  -> executorCores,
      "numExecutors"   -> numExecutors,
      "conf"           -> Json.toJson(conf),
      "name"           -> s"$name-$dateTimeStr"
    )

    val sparkJobParams: JsObject = queue.isEmpty match {
      case false => baseSparkJobParams + ("queue" -> JsString(queue))
      case true  => baseSparkJobParams
    }

    val paramsString = Json.stringify(sparkJobParams)

    val client = HttpClientFactory.getClient()

    val response =
      client.executePost(livyRequestUrl, new StringEntity(paramsString, ContentType.APPLICATION_JSON))

    logger.info(s"Livy response: $response")

  }
}

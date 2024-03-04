package it.agilelab.provisioning.spark.workloads.core.context.cdpPrivate

import it.agilelab.provisioning.spark.workloads.core.context.cdpPrivate.CommonCdpPrivateValidationErrors.NO_NAME_NODE_ERROR
import org.slf4j.LoggerFactory
import play.api.libs.json.{ JsArray, Json }

import java.net.URI

object HdfsClient {

  private val logger = LoggerFactory.getLogger(getClass)

  private val loginContextHttp =
    ApplicationConfigurationCore.provisionerConfig.getString(ApplicationConfigurationCore.LOGIN_CONTEXT)

  def findActiveNameNode(
    nn0: String,
    nn1: String,
    nnPort: String,
    webHdfsProtocol: String,
    client: HttpClientWrapper
  ): Option[String] = {

    val clusters = Seq(
      s"$webHdfsProtocol://$nn0:$nnPort/jmx?qry=Hadoop:service=NameNode,name=NameNodeStatus",
      s"$webHdfsProtocol://$nn1:$nnPort/jmx?qry=Hadoop:service=NameNode,name=NameNodeStatus"
    )

    clusters.find { clusterUrl =>
      val response = client.executeGet(loginContextHttp, clusterUrl)
      logger.info("Trying to get information on the active namenode...")
      logger.info(s"Response from $clusterUrl:\n $response")
      isActiveNameNode(response)
    } match {
      case Some(url) if url.contains(nn0) => Some(nn0)
      case Some(url) if url.contains(nn1) => Some(nn1)
      case None                           => None
    }
  }

  private def isActiveNameNode(response: String): Boolean =
    try {
      (Json.parse(response) \ "beans").as[JsArray].value.exists { bean =>
        (bean \ "State").asOpt[String].contains("active")
      }
    } catch {
      case _: Exception =>
        false
    }

  def jobExists(
    activeNameNode: String,
    nnPort: String,
    client: HttpClientWrapper,
    webHdfsProtocol: String,
    hdfsPath: String
  ): Boolean = {

    val hdfsUri = new URI(hdfsPath)
    val jarPath = hdfsUri.getPath

    val url      = s"$webHdfsProtocol://$activeNameNode:$nnPort/webhdfs/v1$jarPath?op=GETFILESTATUS"
    val response = client.executeGet(loginContextHttp, url)
    try {
      (Json.parse(response) \ "FileStatus" \ "fileId").toOption.isDefined
    } catch {
      case e: Exception =>
        logger.error(
          s"Exception checking the existence of the Spark job at the path '$activeNameNode:$nnPort/webhdfs/v1$jarPath?op=GETFILESTATUS': ${e.getMessage}"
        )
        false
    }
  }

}

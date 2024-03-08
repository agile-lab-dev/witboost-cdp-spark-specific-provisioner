package it.agilelab.provisioning.spark.workloads.core.context.cdpPrivate.httpclient

import org.apache.http.client.methods.{ HttpGet, HttpPost, HttpUriRequest }
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.{ CloseableHttpClient, HttpClients }
import org.apache.http.util.EntityUtils

class NoAuthHttpClient extends HttpClientWrapper {

  private def createClient(): CloseableHttpClient =
    HttpClients.custom().build()

  private def useClient[A](block: CloseableHttpClient => A): A = {
    val client = createClient()
    try block(client)
    finally client.close()
  }

  private def executeRequest(request: HttpUriRequest): String =
    useClient { client =>
      val response = client.execute(request)
      try EntityUtils.toString(response.getEntity)
      finally response.close()
    }

  override def executeGet(url: String): String = {
    val httpGet = new HttpGet(url)
    executeRequest(httpGet)
  }

  override def executePost(url: String, entity: StringEntity): String = {
    val httpPost = new HttpPost(url)
    httpPost.setEntity(entity)
    executeRequest(httpPost)
  }
}

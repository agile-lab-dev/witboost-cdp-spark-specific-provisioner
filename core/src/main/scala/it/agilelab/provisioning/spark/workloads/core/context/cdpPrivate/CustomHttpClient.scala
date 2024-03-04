package it.agilelab.provisioning.spark.workloads.core.context.cdpPrivate

import org.apache.http.auth.AuthSchemeProvider
import org.apache.http.client.config.AuthSchemes
import org.apache.http.client.methods.{ CloseableHttpResponse, HttpGet, HttpPost, HttpUriRequest }
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.config.{ Lookup, RegistryBuilder }
import org.apache.http.entity.StringEntity
import org.apache.http.impl.auth.SPNegoSchemeFactory
import org.apache.http.impl.client.{ BasicCredentialsProvider, CloseableHttpClient, HttpClients }
import org.apache.http.util.EntityUtils
import org.apache.http.auth.{ AuthScope, Credentials }

import java.security.{ Principal, PrivilegedExceptionAction }
import javax.security.auth.Subject
import javax.security.auth.login.LoginContext

class CustomHttpClient extends HttpClientWrapper {

  private def createClient(): CloseableHttpClient = {
    val authSchemeRegistry: Lookup[AuthSchemeProvider] = RegistryBuilder
      .create[AuthSchemeProvider]()
      .register(AuthSchemes.SPNEGO, new SPNegoSchemeFactory(true, false))
      .build()

    HttpClients.custom().setDefaultAuthSchemeRegistry(authSchemeRegistry).build()

  }

  def useClient[A](block: CloseableHttpClient => A): A = {
    val client = createClient()
    try block(client)
    finally client.close()
  }

  private def executeRequest(request: HttpUriRequest, loginContext: String): String = {
    val lc      = new LoginContext(loginContext)
    lc.login()
    val subject = lc.getSubject

    Subject.doAs(
      subject,
      new PrivilegedExceptionAction[String] {
        override def run(): String = {
          val context: HttpClientContext                    = HttpClientContext.create()
          val credentialsProvider: BasicCredentialsProvider = new BasicCredentialsProvider()

          val useJaasCreds: Credentials = new Credentials {
            @SuppressWarnings(Array("org.wartremover.warts.Null"))
            def getPassword: String = null

            @SuppressWarnings(Array("org.wartremover.warts.Null"))
            def getUserPrincipal: Principal = null
          }

          credentialsProvider.setCredentials(
            new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM),
            useJaasCreds
          )
          context.setCredentialsProvider(credentialsProvider)

          useClient { client =>
            val response = client.execute(request, context)
            try EntityUtils.toString(response.getEntity)
            finally response.close()
          }

        }
      }
    )
  }

  override def executeGet(loginContext: String, url: String): String = {
    val httpGet = new HttpGet(url)
    executeRequest(httpGet, loginContext)
  }

  override def executePost(loginContext: String, url: String, entity: StringEntity): String = {
    val httpPost = new HttpPost(url)
    httpPost.setEntity(entity)
    executeRequest(httpPost, loginContext)
  }
}

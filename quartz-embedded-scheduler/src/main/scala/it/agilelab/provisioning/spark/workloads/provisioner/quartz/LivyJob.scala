package it.agilelab.provisioning.spark.workloads.provisioner.quartz

import it.agilelab.provisioning.spark.workloads.provisioner.quartz.config.ApplicationConfiguration
import org.apache.commons.io.IOUtils
import org.apache.http.auth.{ AuthSchemeProvider, AuthScope, Credentials }
import org.apache.http.client.config.AuthSchemes
import org.apache.http.client.methods.{ CloseableHttpResponse, HttpPost }
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.config.{ Lookup, RegistryBuilder }
import org.apache.http.entity.StringEntity
import org.apache.http.impl.auth.SPNegoSchemeFactory
import org.apache.http.impl.client.{ BasicCredentialsProvider, CloseableHttpClient, HttpClients }
import org.apache.http.util.EntityUtils
import org.quartz.{ Job, JobExecutionContext, JobExecutionException }
import org.slf4j.LoggerFactory

import scala.util.Using
import java.security.{ Principal, PrivilegedExceptionAction }
import javax.security.auth.Subject
import javax.security.auth.login.LoginContext

class LivyJob extends Job {

  private val logger = LoggerFactory.getLogger(getClass)

  @throws[JobExecutionException]
  override def execute(context: JobExecutionContext): Unit =
    sendLivyRequest(context)

  def sendLivyRequest(context: JobExecutionContext): Unit = {

    val krb5Conf    = ApplicationConfiguration.provisionerConfig.getString(ApplicationConfiguration.KRB5_CONF_PATH)
    val krbJaasConf = ApplicationConfiguration.provisionerConfig.getString(ApplicationConfiguration.KRB_JAAS_CONF_PATH)

    val _ = System.setProperty("java.security.krb5.conf", krb5Conf)
    val _ = System.setProperty("javax.security.auth.useSubjectCredsOnly", "true")
    val _ = System.setProperty("java.security.auth.login.config", krbJaasConf)

    val livyServerUrl  = context.getScheduler.getContext.get("livyServerUrl")
    val livyRequestUrl = s"$livyServerUrl/batches"

    val data      = context.getJobDetail.getJobDataMap
    val jarPath   = data.getString("jarPath")
    val className = data.getString("className")

    val sparkJobParams = Map(
      "file"      -> jarPath,
      "className" -> className
    )

    try {
      val lc = new LoginContext("Client")
      lc.login()

      Subject.doAs(
        lc.getSubject,
        new PrivilegedExceptionAction[Unit] {
          @throws[Exception]
          def run(): Unit = {
            val authSchemeRegistry: Lookup[AuthSchemeProvider] = RegistryBuilder
              .create[AuthSchemeProvider]()
              .register(AuthSchemes.SPNEGO, new SPNegoSchemeFactory(true, false))
              .build()

            val _ = Using(HttpClients.custom().setDefaultAuthSchemeRegistry(authSchemeRegistry).build()) { client =>
              val context: HttpClientContext                    = HttpClientContext.create()
              val credentialsProvider: BasicCredentialsProvider = new BasicCredentialsProvider()

              val useJaasCreds: Credentials = new Credentials {
                @SuppressWarnings(Array("org.wartremover.warts.Null"))
                def getPassword: String = null

                @SuppressWarnings(Array("org.wartremover.warts.Null"))
                def getUserPrincipal: Principal = null
              }

              val host  = ApplicationConfiguration.provisionerConfig.getString(ApplicationConfiguration.LIVY_HOST)
              val port  = ApplicationConfiguration.provisionerConfig.getInt(ApplicationConfiguration.LIVY_PORT)
              val realm =
                ApplicationConfiguration.provisionerConfig.getString(ApplicationConfiguration.CDP_PRIVATE_REALM)

              credentialsProvider.setCredentials(
                new AuthScope(host, port, realm),
                useJaasCreds
              )
              context.setCredentialsProvider(credentialsProvider)

              val httppost: HttpPost = new HttpPost(livyRequestUrl)
              httppost.setEntity(
                new StringEntity(
                  s"""${sparkJobParams.map { case (k, v) => s""""$k":"$v"""" }.mkString("{", ",", "}")}"""
                )
              )

              val _ = Using(client.execute(httppost, context)) { response =>
                val responseString: String = EntityUtils.toString(response.getEntity, "UTF-8")
                logger.info("HTTP Response:")
                logger.info(responseString)
              }
            }
          }
        }
      )

    } catch {
      case e: Exception => logger.error(s"Failed to submit the Spark job: ${e.getMessage()}", e)

    }
  }

}

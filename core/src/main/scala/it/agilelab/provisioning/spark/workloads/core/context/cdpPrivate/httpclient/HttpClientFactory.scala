package it.agilelab.provisioning.spark.workloads.core.context.cdpPrivate.httpclient

import it.agilelab.provisioning.spark.workloads.core.context.cdpPrivate.ApplicationConfigurationCore

object HttpClientFactory {
  def getClient(): HttpClientWrapper = {

    val useKerberosAuth =
      ApplicationConfigurationCore.provisionerConfig.getString(ApplicationConfigurationCore.USE_KERBEROS_AUTH)

    useKerberosAuth match {
      case "true" =>
        val loginContextHttp =
          ApplicationConfigurationCore.provisionerConfig.getString(ApplicationConfigurationCore.LOGIN_CONTEXT)
        new KerberosHttpClient(loginContextHttp)
      case _      => new NoAuthHttpClient()
    }
  }
}

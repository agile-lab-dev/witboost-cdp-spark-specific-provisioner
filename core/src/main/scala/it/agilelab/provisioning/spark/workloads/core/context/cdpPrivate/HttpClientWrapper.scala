package it.agilelab.provisioning.spark.workloads.core.context.cdpPrivate

import org.apache.http.entity.StringEntity

trait HttpClientWrapper {
  def executeGet(loginContext: String, url: String): String
  def executePost(loginContext: String, url: String, entity: StringEntity): String
}

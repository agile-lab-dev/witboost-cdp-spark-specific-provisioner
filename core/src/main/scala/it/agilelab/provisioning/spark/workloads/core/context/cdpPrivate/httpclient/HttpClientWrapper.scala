package it.agilelab.provisioning.spark.workloads.core.context.cdpPrivate.httpclient

import org.apache.http.entity.StringEntity

trait HttpClientWrapper {
  def executeGet(url: String): String

  def executePost(url: String, entity: StringEntity): String
}

package it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.mapper

import java.security.MessageDigest
import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.base.Resource
import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.request.CreateResourceReq

trait SparkWorkloadMapper {
  def defineResourceName(prefix: String, jobName: String): String =
    s"$prefix-${md5(jobName)}"

  def createFileResourceReq(resourceName: String): CreateResourceReq = {
    val res = Resource.filesResource(resourceName)
    CreateResourceReq(res.name, res.`type`, res.retentionPolicy, None)
  }

  def createEnvResourceReq(
    resourceName: String,
    pyVersion: String,
    pypiMirror: Option[String]
  ): CreateResourceReq = {
    val res = Resource.environmentResource(resourceName, pyVersion, pypiMirror)
    CreateResourceReq(res.name, res.`type`, res.retentionPolicy, Some(res.pythonEnvironment))
  }

  def destArtifactPath(path: String): String = path.split("/").last

  def md5(value: String): String =
    MessageDigest
      .getInstance("MD5")
      .digest(value.getBytes("UTF-8"))
      .map("%02x".format(_))
      .mkString
}

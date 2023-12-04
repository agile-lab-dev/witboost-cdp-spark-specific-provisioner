package it.agilelab.provisioning.spark.workloads.provisioner.app.api.validate

import it.agilelab.provisioning.mesh.self.service.api.model.Component.Workload
import it.agilelab.provisioning.mesh.self.service.api.model.DataProduct
import io.circe.Json
import it.agilelab.provisioning.spark.workload.core.SparkCde
import it.agilelab.provisioning.spark.workload.core.models.DpCdp

object SparkJobNamingValidator {
  private val COMPLIANT_NAME_REGEX = "[^a-zA-Z0-9-_]"

  def isJobNameValid(dp: DataProduct[DpCdp], cmp: Workload[SparkCde]): Boolean = {
    val prefix: String = getJobNamePrefix(dp).replaceAll(COMPLIANT_NAME_REGEX, "-")
    val suffix: String = getJobNameSuffix(dp).replaceAll(COMPLIANT_NAME_REGEX, "-")
    cmp.name.startsWith(prefix) &&
    cmp.name.endsWith(suffix) &&
    isNameCompliant(cmp.name) &&
    cmp.name.length > (prefix.length + suffix.length)
  }

  private def getJobNamePrefix(dp: DataProduct[DpCdp]): String =
    String.join("-", dp.domain, dp.name, getDpMajorVersion(dp)) + "-"

  private def getJobNameSuffix(dp: DataProduct[DpCdp]): String =
    "-" + dp.environment

  private def getDpMajorVersion(dp: DataProduct[DpCdp]): String =
    dp.version.split("\\.").headOption.getOrElse(dp.version)

  private def isNameCompliant(name: String): Boolean = COMPLIANT_NAME_REGEX.r.findFirstIn(name) match {
    case Some(_) => false
    case None    => true
  }

}

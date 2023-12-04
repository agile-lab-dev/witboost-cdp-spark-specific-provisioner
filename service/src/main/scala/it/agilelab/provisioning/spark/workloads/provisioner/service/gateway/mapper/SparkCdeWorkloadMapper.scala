package it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.mapper

import cats.implicits.{ showInterpolator, toBifunctorOps, toTraverseOps }
import com.cloudera.cdp.de.model.{ ServiceDescription, VcDescription }
import it.agilelab.provisioning.commons.client.cdp.de.CdpDeClient
import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.base.File.{ file, jar }
import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.base.Job.{ pyspark, spark }
import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.base.Schedule.enable
import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.base.{ File, Resource, Schedule }
import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.request.{ CreateResourceReq, UploadFileReq }
import it.agilelab.provisioning.mesh.self.service.api.model.Component.Workload
import it.agilelab.provisioning.mesh.self.service.api.model.DataProduct
import it.agilelab.provisioning.mesh.self.service.core.gateway.ComponentGatewayError
import it.agilelab.provisioning.spark.workload.core.SparkCde
import it.agilelab.provisioning.spark.workload.core.SparkCde._
import it.agilelab.provisioning.spark.workload.core.models.DpCdp
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.artifactory.{
  ArtifactoryGateway,
  ArtifactoryGatewayError
}
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.idbroker.IdBrokerHostsProvider
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.workload.SparkCdeWorkload

import java.security.MessageDigest

class SparkCdeWorkloadMapper(
  cdpDeClient: CdpDeClient,
  idBrokerHostsProvider: IdBrokerHostsProvider,
  artifactoryGateway: ArtifactoryGateway
) {
  def map(
    dataProduct: DataProduct[DpCdp],
    workload: Workload[SparkCde]
  ): Either[ComponentGatewayError, SparkCdeWorkload] =
    for {
      service        <- cdpDeClient
                          .describeServiceByName(workload.specific.cdeService)
                          .leftMap(error => ComponentGatewayError(show"$error"))
      virtualCluster <- cdpDeClient
                          .describeVcByName(service.getClusterId, workload.specific.cdeCluster)
                          .leftMap(error => ComponentGatewayError(show"$error"))
      idBrokerHosts  <- idBrokerHostsProvider
                          .get(service)
                          .leftMap(error => ComponentGatewayError(show"$error"))
      mainResource    = defineResourceName("mainres", workload.specific.jobName)
      fileRename      = createFileResourceReq(mainResource)
      schedule        = workload.specific.jobConfig.flatMap(c =>
                          c.schedule.map(w => enable(user = "", w.cronExpression, w.startDate, w.endDate))
                        )
      conf            = Map("spark.cde.idBrokerHosts" -> idBrokerHosts.mkString(","))
      sparkWorkload  <- createSparkCdeWorkloadJob(
                          dataProduct,
                          workload,
                          service,
                          virtualCluster,
                          mainResource,
                          fileRename,
                          schedule,
                          conf
                        )
                          .leftMap(error => ComponentGatewayError(show"$error"))
    } yield sparkWorkload

  private def createSparkCdeWorkloadJob(
    dataProduct: DataProduct[DpCdp],
    workload: Workload[SparkCde],
    serviceDescription: ServiceDescription,
    vcDescription: VcDescription,
    mainResource: String,
    mainResourceReq: CreateResourceReq,
    schedule: Option[Schedule],
    idBrokerHostConf: Map[String, String]
  ): Either[ArtifactoryGatewayError, SparkCdeWorkload] =
    workload.specific match {
      case SparkCdeJob(_, _, jn, appFile, className, jobConfig) =>
        for {
          artifacts <- uploadFileReq(mainResource, appFile, jobConfig.flatMap(_.dependencies), jar)
        } yield SparkCdeWorkload(
          dataProduct.domain,
          dataProduct.name,
          serviceDescription,
          vcDescription,
          Seq(mainResourceReq),
          artifacts,
          spark(
            jn,
            mainResource,
            destArtifactPath(appFile),
            className,
            jobConfig.flatMap(_.dependencies).map(d => d.map(destArtifactPath)),
            jobConfig.flatMap(_.args),
            jobConfig.flatMap(_.driverCores),
            jobConfig.flatMap(_.driverMemory),
            jobConfig.flatMap(_.executorCores),
            jobConfig.flatMap(_.executorMemory),
            jobConfig.flatMap(_.numExecutors),
            schedule = schedule,
            logLevel = jobConfig.flatMap(_.logLevel),
            conf = jobConfig.flatMap(_.conf.map(_ ++ idBrokerHostConf)).orElse(Some(idBrokerHostConf))
          )
        )

      case PySparkCdeJob(_, _, jn, pyFile, pyV, jobConfig)                             =>
        for {
          artifacts <- uploadFileReq(mainResource, pyFile, jobConfig.flatMap(_.dependencies), file)
        } yield SparkCdeWorkload(
          dataProduct.domain,
          dataProduct.name,
          serviceDescription,
          vcDescription,
          Seq(mainResourceReq),
          artifacts,
          pyspark(
            jn,
            mainResource,
            destArtifactPath(pyFile),
            jobConfig.flatMap(_.args),
            None,
            jobConfig.flatMap(_.dependencies.map(d => d.map(destArtifactPath))),
            jobConfig.flatMap(_.driverCores),
            jobConfig.flatMap(_.driverMemory),
            jobConfig.flatMap(_.executorCores),
            jobConfig.flatMap(_.executorMemory),
            jobConfig.flatMap(_.numExecutors),
            schedule = schedule,
            logLevel = jobConfig.flatMap(_.logLevel),
            conf = jobConfig
              .flatMap(_.conf.map(_ ++ idBrokerHostConf ++ Map("spark.pyspark.python" -> pyV)))
              .orElse(Some(idBrokerHostConf ++ Map("spark.pyspark.python" -> pyV)))
          )
        )
      case PySparkCdeJobWithPyEnv(_, _, jn, pyFile, pyV, reqFile, pyMirror, jobConfig) =>
        for {
          artifacts <- uploadFileReq(mainResource, pyFile, jobConfig.flatMap(_.dependencies), file)
          eRes       = createEnvResourceReq(defineResourceName("pyenvres", jn), pyV, pyMirror)
          eArt      <- uploadFileReq(eRes.name, reqFile, None, file)
        } yield SparkCdeWorkload(
          dataProduct.domain,
          dataProduct.name,
          serviceDescription,
          vcDescription,
          Seq(mainResourceReq, eRes),
          artifacts ++ eArt,
          pyspark(
            jn,
            mainResource,
            destArtifactPath(pyFile),
            jobConfig.flatMap(_.args),
            Some(eRes.name),
            jobConfig.flatMap(_.dependencies.map(d => d.map(destArtifactPath))),
            jobConfig.flatMap(_.driverCores),
            jobConfig.flatMap(_.driverMemory),
            jobConfig.flatMap(_.executorCores),
            jobConfig.flatMap(_.executorMemory),
            jobConfig.flatMap(_.numExecutors),
            schedule = schedule,
            logLevel = jobConfig.flatMap(_.logLevel),
            conf = jobConfig
              .flatMap(_.conf.map(_ ++ idBrokerHostConf ++ Map("spark.pyspark.python" -> pyV)))
              .orElse(Some(idBrokerHostConf ++ Map("spark.pyspark.python" -> pyV)))
          )
        )
    }

  private def defineResourceName(prefix: String, jobName: String): String =
    s"$prefix-${md5(jobName)}"

  private def createFileResourceReq(resourceName: String): CreateResourceReq = {
    val res = Resource.filesResource(resourceName)
    CreateResourceReq(res.name, res.`type`, res.retentionPolicy, None)
  }

  private def createEnvResourceReq(
    resourceName: String,
    pyVersion: String,
    pypiMirror: Option[String]
  ): CreateResourceReq = {
    val res = Resource.environmentResource(resourceName, pyVersion, pypiMirror)
    CreateResourceReq(res.name, res.`type`, res.retentionPolicy, Some(res.pythonEnvironment))
  }

  def uploadFileReq(
    resource: String,
    app: String,
    dependencies: Option[Seq[String]],
    fileFun: (String, String, Array[Byte]) => File
  ): Either[ArtifactoryGatewayError, Seq[UploadFileReq]] =
    dependencies
      .fold(Seq(app))(deps => Seq(app) ++ deps)
      .map { d =>
        artifactoryGateway
          .get(d)
          .map { f =>
            val j = fileFun(resource, destArtifactPath(d), f)
            UploadFileReq(j.resource, j.filePath, j.mimeType, j.file)
          }
      }
      .sequence

  private def destArtifactPath(path: String): String =
    path.split("/").last

  private def md5(value: String): String =
    MessageDigest
      .getInstance("MD5")
      .digest(value.getBytes("UTF-8"))
      .map("%02x".format(_))
      .mkString

}

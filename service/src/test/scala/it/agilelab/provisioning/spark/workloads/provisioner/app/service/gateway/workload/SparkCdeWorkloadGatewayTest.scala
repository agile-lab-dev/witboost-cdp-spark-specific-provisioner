package it.agilelab.provisioning.spark.workloads.provisioner.app.service.gateway.workload

import com.cloudera.cdp.de.model.{ ServiceDescription, VcDescription }
import it.agilelab.provisioning.commons.client.cdp.de.CdpDeClient
import it.agilelab.provisioning.commons.client.cdp.de.cluster.CdeClusterClient
import it.agilelab.provisioning.commons.client.cdp.de.cluster.CdeClusterClientError.{
  CreateResourceErr,
  DeleteJobErr,
  DeleteResourceErr,
  GetJobErr,
  UpdateJobErr,
  UploadFileErr
}
import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.base.{ Job, JobDetails, Mount, SparkJob }
import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.request._
import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.response.GetJobRes
import it.agilelab.provisioning.commons.http.HttpErrors.{ GenericErr, ServerErr }
import it.agilelab.provisioning.spark.workload.core.SparkWorkloadResponse
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.artifactory.ArtifactoryGateway
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.clients.CdeClientFactory
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.credential.CredentialProviderError._
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.idbroker.IdBrokerHostsProvider
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.mapper.{
  SparkCdeWorkloadMapper,
  SparkCdpPrivateWorkloadMapper
}
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.workload.{
  SparkCdeWorkload,
  SparkCdeWorkloadGateway
}
import org.scalamock.scalatest.MockFactory
import org.scalatest.EitherValues
import org.scalatest.funsuite.AnyFunSuite

class SparkCdeWorkloadGatewayTest extends AnyFunSuite with MockFactory with EitherValues {

  val cdpDeClient: CdpDeClient                    = mock[CdpDeClient]
  val idBrokerHostProvider: IdBrokerHostsProvider = mock[IdBrokerHostsProvider]
  val artifactoryGateway: ArtifactoryGateway      = mock[ArtifactoryGateway]
  val cdeClientFactory: CdeClientFactory          = mock[CdeClientFactory]
  val cdeClusterClient: CdeClusterClient          = mock[CdeClusterClient]

  val myLogicMapper    = new SparkCdeWorkloadMapper(cdpDeClient, idBrokerHostProvider, artifactoryGateway)
  val sparkJobDeployer = new SparkCdeWorkloadGateway(cdeClientFactory)

  val srvDesc = new ServiceDescription()
  srvDesc.setName("serviceXX")
  srvDesc.setClusterId("serviceXX-id")
  srvDesc.setClusterFqdn("my-cluster-fqdn")

  val vcDesc = new VcDescription()
  vcDesc.setClusterId("serviceXX-id")
  vcDesc.setVcName("clusterXX")
  vcDesc.setVcId("clusterXX-id")
  vcDesc.setVcApiUrl("https://my-api")

  test("deploy return Right(SparkWorkloadGateway)") {
    inSequence(
      (cdeClientFactory.create _)
        .expects("my-dp-domain", "my-dp-name")
        .once()
        .returns(Right(cdeClusterClient)),
      (cdeClusterClient.safeCreateResource _)
        .expects(CdeRequest(srvDesc, vcDesc, CreateResourceReq("myres1", "myrestype1", "myretpol1", None)))
        .returns(Right()),
      (cdeClusterClient.safeCreateResource _)
        .expects(CdeRequest(srvDesc, vcDesc, CreateResourceReq("myres2", "myrestype2", "myretpol2", None)))
        .returns(Right()),
      (cdeClusterClient.uploadFile _)
        .expects(CdeRequest(srvDesc, vcDesc, UploadFileReq("myres1", "myfile1", "mime1", Array.emptyByteArray)))
        .returns(Right()),
      (cdeClusterClient.uploadFile _)
        .expects(CdeRequest(srvDesc, vcDesc, UploadFileReq("myres2", "myfile2", "mime2", Array.emptyByteArray)))
        .returns(Right()),
      (cdeClusterClient.uploadFile _)
        .expects(CdeRequest(srvDesc, vcDesc, UploadFileReq("myres2", "myfile3", "mime3", Array.emptyByteArray)))
        .returns(Right()),
      (cdeClusterClient.upsertJob _)
        .expects(
          CdeRequest(
            srvDesc,
            vcDesc,
            UpsertJobReq(Job.spark("y", "y", "z", "cn", None, None, None, None, None, None, None, None, None, None))
          )
        )
        .returns(Right()),
      (cdeClusterClient.getJob _)
        .expects(CdeRequest(srvDesc, vcDesc, GetJobReq("y")))
        .returns(
          Right(
            GetJobRes(
              Some(
                JobDetails(
                  name = "jobname",
                  `type` = "spark",
                  created = "createdAt",
                  modified = "modifiedAt",
                  lastUsed = "lastUsedAt",
                  mounts = Seq(Mount("jobres")),
                  retentionPolicy = "keep_indefinitely",
                  spark = Some(
                    SparkJob.defaultSparkJob(
                      file = "workloadJarKey.jar",
                      className = "it.agilelab.classPath.MyClass"
                    )
                  ),
                  schedule = None
                )
              )
            )
          )
        )
    )

    val actual = sparkJobDeployer.deployJob(
      SparkCdeWorkload(
        "my-dp-domain",
        "my-dp-name",
        srvDesc,
        vcDesc,
        Seq(
          CreateResourceReq("myres1", "myrestype1", "myretpol1", None),
          CreateResourceReq("myres2", "myrestype2", "myretpol2", None)
        ),
        Seq(
          UploadFileReq("myres1", "myfile1", "mime1", Array.emptyByteArray),
          UploadFileReq("myres2", "myfile2", "mime2", Array.emptyByteArray),
          UploadFileReq("myres2", "myfile3", "mime3", Array.emptyByteArray)
        ),
        Job.spark("y", "y", "z", "cn", None, None, None, None, None, None, None, None, None, None)
      )
    )

    val expected = Right(
      SparkWorkloadResponse(
        Some(
          JobDetails(
            name = "jobname",
            `type` = "spark",
            created = "createdAt",
            modified = "modifiedAt",
            lastUsed = "lastUsedAt",
            mounts = Seq(Mount("jobres")),
            retentionPolicy = "keep_indefinitely",
            spark = Some(
              SparkJob.defaultSparkJob(
                file = "workloadJarKey.jar",
                className = "it.agilelab.classPath.MyClass"
              )
            ),
            schedule = None
          )
        ),
        None
      )
    )
    assert(actual == expected)
  }

  test("deploy return Left(ComponentGatewayError) on cdeClientFactory left") {
    (cdeClientFactory.create _)
      .expects("my-dp-domain", "my-dp-name")
      .once()
      .returns(Left(CredentialDomainNotFoundErr("domain")))

    val actual = sparkJobDeployer.deployJob(
      SparkCdeWorkload(
        "my-dp-domain",
        "my-dp-name",
        srvDesc,
        vcDesc,
        Seq(
          CreateResourceReq("myres1", "myrestype1", "myretpol1", None),
          CreateResourceReq("myres2", "myrestype2", "myretpol2", None)
        ),
        Seq(
          UploadFileReq("myres1", "myfile1", "mime1", Array.emptyByteArray),
          UploadFileReq("myres2", "myfile2", "mime2", Array.emptyByteArray),
          UploadFileReq("myres2", "myfile3", "mime3", Array.emptyByteArray)
        ),
        Job.spark("y", "y", "z", "cn", None, None, None, None, None, None, None, None, None, None)
      )
    )
    assert(actual.isLeft)
    assert(actual.left.value.error == "CredentialDomainNotFoundErr(domain)")
  }

  test("deploy return Left(ComponentGatewayError) on create resource left") {
    inSequence(
      (cdeClientFactory.create _)
        .expects("my-dp-domain", "my-dp-name")
        .once()
        .returns(Right(cdeClusterClient)),
      (cdeClusterClient.safeCreateResource _)
        .expects(CdeRequest(srvDesc, vcDesc, CreateResourceReq("myres1", "myrestype1", "myretpol1", None)))
        .returns(Right()),
      (cdeClusterClient.safeCreateResource _)
        .expects(CdeRequest(srvDesc, vcDesc, CreateResourceReq("myres2", "myrestype2", "myretpol2", None)))
        .returns(
          Left(CreateResourceErr(CreateResourceReq("myres2", "myrestype2", "myretpol2", None), GenericErr(500, "x")))
        )
    )

    val actual = sparkJobDeployer.deployJob(
      SparkCdeWorkload(
        "my-dp-domain",
        "my-dp-name",
        srvDesc,
        vcDesc,
        Seq(
          CreateResourceReq("myres1", "myrestype1", "myretpol1", None),
          CreateResourceReq("myres2", "myrestype2", "myretpol2", None)
        ),
        Seq(
          UploadFileReq("myres1", "myfile1", "mime1", Array.emptyByteArray),
          UploadFileReq("myres2", "myfile2", "mime2", Array.emptyByteArray),
          UploadFileReq("myres2", "myfile3", "mime3", Array.emptyByteArray)
        ),
        Job.spark("y", "y", "z", "cn", None, None, None, None, None, None, None, None, None, None)
      )
    )

    assert(actual.isLeft)
    assert(actual.left.value.error.startsWith("CreateResourceErr(CreateResourceReq"))
  }

  test("deploy return Left(ComponentGatewayError) on uploadFile left") {
    inSequence(
      (cdeClientFactory.create _)
        .expects("my-dp-domain", "my-dp-name")
        .once()
        .returns(Right(cdeClusterClient)),
      (cdeClusterClient.safeCreateResource _)
        .expects(CdeRequest(srvDesc, vcDesc, CreateResourceReq("myres1", "myrestype1", "myretpol1", None)))
        .returns(Right()),
      (cdeClusterClient.safeCreateResource _)
        .expects(CdeRequest(srvDesc, vcDesc, CreateResourceReq("myres2", "myrestype2", "myretpol2", None)))
        .returns(Right()),
      (cdeClusterClient.uploadFile _)
        .expects(CdeRequest(srvDesc, vcDesc, UploadFileReq("myres1", "myfile1", "mime1", Array.emptyByteArray)))
        .returns(Right()),
      (cdeClusterClient.uploadFile _)
        .expects(CdeRequest(srvDesc, vcDesc, UploadFileReq("myres2", "myfile2", "mime2", Array.emptyByteArray)))
        .returns(Right()),
      (cdeClusterClient.uploadFile _)
        .expects(CdeRequest(srvDesc, vcDesc, UploadFileReq("myres2", "myfile3", "mime3", Array.emptyByteArray)))
        .returns(
          Left(UploadFileErr(UploadFileReq("myres2", "myfile3", "mime3", Array.emptyByteArray), GenericErr(500, "x")))
        )
    )

    val actual = sparkJobDeployer.deployJob(
      SparkCdeWorkload(
        "my-dp-domain",
        "my-dp-name",
        srvDesc,
        vcDesc,
        Seq(
          CreateResourceReq("myres1", "myrestype1", "myretpol1", None),
          CreateResourceReq("myres2", "myrestype2", "myretpol2", None)
        ),
        Seq(
          UploadFileReq("myres1", "myfile1", "mime1", Array.emptyByteArray),
          UploadFileReq("myres2", "myfile2", "mime2", Array.emptyByteArray),
          UploadFileReq("myres2", "myfile3", "mime3", Array.emptyByteArray)
        ),
        Job.spark("y", "y", "z", "cn", None, None, None, None, None, None, None, None, None, None)
      )
    )

    assert(actual.isLeft)
    assert(actual.left.value.error.startsWith("UploadFileErr(UploadFileReq"))
  }

  test("deploy return Left(ComponentGatewayError) on upsert job left") {
    inSequence(
      (cdeClientFactory.create _)
        .expects("my-dp-domain", "my-dp-name")
        .once()
        .returns(Right(cdeClusterClient)),
      (cdeClusterClient.safeCreateResource _)
        .expects(CdeRequest(srvDesc, vcDesc, CreateResourceReq("myres1", "myrestype1", "myretpol1", None)))
        .returns(Right()),
      (cdeClusterClient.safeCreateResource _)
        .expects(CdeRequest(srvDesc, vcDesc, CreateResourceReq("myres2", "myrestype2", "myretpol2", None)))
        .returns(Right()),
      (cdeClusterClient.uploadFile _)
        .expects(CdeRequest(srvDesc, vcDesc, UploadFileReq("myres1", "myfile1", "mime1", Array.emptyByteArray)))
        .returns(Right()),
      (cdeClusterClient.uploadFile _)
        .expects(CdeRequest(srvDesc, vcDesc, UploadFileReq("myres2", "myfile2", "mime2", Array.emptyByteArray)))
        .returns(Right()),
      (cdeClusterClient.uploadFile _)
        .expects(CdeRequest(srvDesc, vcDesc, UploadFileReq("myres2", "myfile3", "mime3", Array.emptyByteArray)))
        .returns(Right()),
      (cdeClusterClient.upsertJob _)
        .expects(
          CdeRequest(
            srvDesc,
            vcDesc,
            UpsertJobReq(Job.spark("y", "y", "z", "cn", None, None, None, None, None, None, None, None, None, None))
          )
        )
        .returns(
          Left(
            UpdateJobErr(
              UpdateJobReq(Job.spark("y", "y", "z", "cn", None, None, None, None, None, None, None, None, None, None)),
              GenericErr(500, "x")
            )
          )
        )
    )

    val actual = sparkJobDeployer.deployJob(
      gateway.workload.SparkCdeWorkload(
        "my-dp-domain",
        "my-dp-name",
        srvDesc,
        vcDesc,
        Seq(
          CreateResourceReq("myres1", "myrestype1", "myretpol1", None),
          CreateResourceReq("myres2", "myrestype2", "myretpol2", None)
        ),
        Seq(
          UploadFileReq("myres1", "myfile1", "mime1", Array.emptyByteArray),
          UploadFileReq("myres2", "myfile2", "mime2", Array.emptyByteArray),
          UploadFileReq("myres2", "myfile3", "mime3", Array.emptyByteArray)
        ),
        Job.spark("y", "y", "z", "cn", None, None, None, None, None, None, None, None, None, None)
      )
    )

    assert(actual.isLeft)
    assert(actual.left.value.error.startsWith("UpdateJobErr(UpdateJobReq"))
  }

  test("deploy return Left(ComponentGatewayError) on get job left") {
    inSequence(
      (cdeClientFactory.create _)
        .expects("my-dp-domain", "my-dp-name")
        .once()
        .returns(Right(cdeClusterClient)),
      (cdeClusterClient.safeCreateResource _)
        .expects(CdeRequest(srvDesc, vcDesc, CreateResourceReq("myres1", "myrestype1", "myretpol1", None)))
        .returns(Right()),
      (cdeClusterClient.safeCreateResource _)
        .expects(CdeRequest(srvDesc, vcDesc, CreateResourceReq("myres2", "myrestype2", "myretpol2", None)))
        .returns(Right()),
      (cdeClusterClient.uploadFile _)
        .expects(CdeRequest(srvDesc, vcDesc, UploadFileReq("myres1", "myfile1", "mime1", Array.emptyByteArray)))
        .returns(Right()),
      (cdeClusterClient.uploadFile _)
        .expects(CdeRequest(srvDesc, vcDesc, UploadFileReq("myres2", "myfile2", "mime2", Array.emptyByteArray)))
        .returns(Right()),
      (cdeClusterClient.uploadFile _)
        .expects(CdeRequest(srvDesc, vcDesc, UploadFileReq("myres2", "myfile3", "mime3", Array.emptyByteArray)))
        .returns(Right()),
      (cdeClusterClient.upsertJob _)
        .expects(
          CdeRequest(
            srvDesc,
            vcDesc,
            UpsertJobReq(Job.spark("y", "y", "z", "cn", None, None, None, None, None, None, None, None, None, None))
          )
        )
        .returns(Right()),
      (cdeClusterClient.getJob _)
        .expects(CdeRequest(srvDesc, vcDesc, GetJobReq("y")))
        .returns(Left(GetJobErr(GetJobReq("x"), GenericErr(500, "x"))))
    )

    val actual = sparkJobDeployer.deployJob(
      gateway.workload.SparkCdeWorkload(
        "my-dp-domain",
        "my-dp-name",
        srvDesc,
        vcDesc,
        Seq(
          CreateResourceReq("myres1", "myrestype1", "myretpol1", None),
          CreateResourceReq("myres2", "myrestype2", "myretpol2", None)
        ),
        Seq(
          UploadFileReq("myres1", "myfile1", "mime1", Array.emptyByteArray),
          UploadFileReq("myres2", "myfile2", "mime2", Array.emptyByteArray),
          UploadFileReq("myres2", "myfile3", "mime3", Array.emptyByteArray)
        ),
        Job.spark("y", "y", "z", "cn", None, None, None, None, None, None, None, None, None, None)
      )
    )

    assert(actual.isLeft)
    assert(actual.left.value.error.startsWith("GetJobErr(GetJobReq"))
  }

  test("undeploy return Right(SparkWorkloadGateway)") {
    inSequence(
      (cdeClientFactory.create _)
        .expects("my-dp-domain", "my-dp-name")
        .once()
        .returns(Right(cdeClusterClient)),
      (cdeClusterClient.safeDeleteJob _)
        .expects(
          CdeRequest(
            srvDesc,
            vcDesc,
            DeleteJobReq("y")
          )
        )
        .returns(Right()),
      (cdeClusterClient.safeDeleteResource _)
        .expects(CdeRequest(srvDesc, vcDesc, DeleteResourceReq("myres1")))
        .returns(Right()),
      (cdeClusterClient.safeDeleteResource _)
        .expects(CdeRequest(srvDesc, vcDesc, DeleteResourceReq("myres2")))
        .returns(Right()),
      (cdeClusterClient.getJob _)
        .expects(CdeRequest(srvDesc, vcDesc, GetJobReq("y")))
        .returns(
          Right(
            GetJobRes(
              None
            )
          )
        )
    )

    val actual = sparkJobDeployer.undeployJob(
      SparkCdeWorkload(
        "my-dp-domain",
        "my-dp-name",
        srvDesc,
        vcDesc,
        Seq(
          CreateResourceReq("myres1", "myrestype1", "myretpol1", None),
          CreateResourceReq("myres2", "myrestype2", "myretpol2", None)
        ),
        Seq(
          UploadFileReq("myres1", "myfile1", "mime1", Array.emptyByteArray),
          UploadFileReq("myres2", "myfile2", "mime2", Array.emptyByteArray),
          UploadFileReq("myres2", "myfile3", "mime3", Array.emptyByteArray)
        ),
        Job.spark("y", "y", "z", "cn", None, None, None, None, None, None, None, None, None, None)
      )
    )

    val expected = Right(
      SparkWorkloadResponse(
        None,
        Some(
          Job.spark("y", "y", "z", "cn", None, None, None, None, None, None, None, None, None, None)
        )
      )
    )
    assert(actual == expected)
  }

  test("undeploy return Left(ComponentGatewayError) on cdeClientFactory left") {
    (cdeClientFactory.create _)
      .expects("my-dp-domain", "my-dp-name")
      .once()
      .returns(Left(CredentialDomainNotFoundErr("domain")))

    val actual = sparkJobDeployer.undeployJob(
      gateway.workload.SparkCdeWorkload(
        "my-dp-domain",
        "my-dp-name",
        srvDesc,
        vcDesc,
        Seq(
          CreateResourceReq("myres1", "myrestype1", "myretpol1", None),
          CreateResourceReq("myres2", "myrestype2", "myretpol2", None)
        ),
        Seq(
          UploadFileReq("myres1", "myfile1", "mime1", Array.emptyByteArray),
          UploadFileReq("myres2", "myfile2", "mime2", Array.emptyByteArray),
          UploadFileReq("myres2", "myfile3", "mime3", Array.emptyByteArray)
        ),
        Job.spark("y", "y", "z", "cn", None, None, None, None, None, None, None, None, None, None)
      )
    )
    assert(actual.isLeft)
    assert(actual.left.value.error == "CredentialDomainNotFoundErr(domain)")
  }

  test("undeploy return Left(ComponentGatewayError) on delete resource left") {
    (cdeClientFactory.create _)
      .expects("my-dp-domain", "my-dp-name")
      .once()
      .returns(Right(cdeClusterClient))

    (cdeClusterClient.safeDeleteJob _)
      .expects(
        CdeRequest(
          srvDesc,
          vcDesc,
          DeleteJobReq("y")
        )
      )
      .returns(Right())

    (cdeClusterClient.safeDeleteResource _)
      .expects(CdeRequest(srvDesc, vcDesc, DeleteResourceReq("myres1")))
      .returns(Left(DeleteResourceErr(DeleteResourceReq("myres1"), ServerErr(500, "x"))))

    (cdeClusterClient.safeDeleteResource _)
      .expects(CdeRequest(srvDesc, vcDesc, DeleteResourceReq("myres2")))
      .returns(Left(DeleteResourceErr(DeleteResourceReq("myres2"), ServerErr(500, "x"))))

    val actual = sparkJobDeployer.undeployJob(
      gateway.workload.SparkCdeWorkload(
        "my-dp-domain",
        "my-dp-name",
        srvDesc,
        vcDesc,
        Seq(
          CreateResourceReq("myres1", "myrestype1", "myretpol1", None),
          CreateResourceReq("myres2", "myrestype2", "myretpol2", None)
        ),
        Seq(
          UploadFileReq("myres1", "myfile1", "mime1", Array.emptyByteArray),
          UploadFileReq("myres2", "myfile2", "mime2", Array.emptyByteArray),
          UploadFileReq("myres2", "myfile3", "mime3", Array.emptyByteArray)
        ),
        Job.spark("y", "y", "z", "cn", None, None, None, None, None, None, None, None, None, None)
      )
    )
    assert(actual.isLeft)
    assert(actual.left.value.error == "DeleteResourceErr(DeleteResourceReq(myres1),ServerErr(500,x))")
  }

  test("undeploy return Left(ComponentGatewayError) on delete job left") {
    inSequence(
      (cdeClientFactory.create _)
        .expects("my-dp-domain", "my-dp-name")
        .once()
        .returns(Right(cdeClusterClient)),
      (cdeClusterClient.safeDeleteJob _)
        .expects(
          CdeRequest(
            srvDesc,
            vcDesc,
            DeleteJobReq("y")
          )
        )
        .returns(
          Left(
            DeleteJobErr(
              DeleteJobReq("y"),
              GenericErr(500, "x")
            )
          )
        )
    )

    val actual = sparkJobDeployer.undeployJob(
      gateway.workload.SparkCdeWorkload(
        "my-dp-domain",
        "my-dp-name",
        srvDesc,
        vcDesc,
        Seq(
          CreateResourceReq("myres1", "myrestype1", "myretpol1", None),
          CreateResourceReq("myres2", "myrestype2", "myretpol2", None)
        ),
        Seq(
          UploadFileReq("myres1", "myfile1", "mime1", Array.emptyByteArray),
          UploadFileReq("myres2", "myfile2", "mime2", Array.emptyByteArray),
          UploadFileReq("myres2", "myfile3", "mime3", Array.emptyByteArray)
        ),
        Job.spark("y", "y", "z", "cn", None, None, None, None, None, None, None, None, None, None)
      )
    )

    assert(actual.isLeft)
    assert(actual.left.value.error.startsWith("DeleteJobErr(DeleteJobReq"))
  }

  test("undeploy return Left(ComponentGatewayError) on get job left") {
    inSequence(
      (cdeClientFactory.create _)
        .expects("my-dp-domain", "my-dp-name")
        .once()
        .returns(Right(cdeClusterClient)),
      (cdeClusterClient.safeDeleteJob _)
        .expects(
          CdeRequest(
            srvDesc,
            vcDesc,
            DeleteJobReq("y")
          )
        )
        .returns(Right()),
      (cdeClusterClient.safeDeleteResource _)
        .expects(CdeRequest(srvDesc, vcDesc, DeleteResourceReq("myres1")))
        .returns(Right()),
      (cdeClusterClient.safeDeleteResource _)
        .expects(CdeRequest(srvDesc, vcDesc, DeleteResourceReq("myres2")))
        .returns(Right()),
      (cdeClusterClient.getJob _)
        .expects(CdeRequest(srvDesc, vcDesc, GetJobReq("y")))
        .returns(Left(GetJobErr(GetJobReq("x"), GenericErr(500, "x"))))
    )

    val actual = sparkJobDeployer.undeployJob(
      gateway.workload.SparkCdeWorkload(
        "my-dp-domain",
        "my-dp-name",
        srvDesc,
        vcDesc,
        Seq(
          CreateResourceReq("myres1", "myrestype1", "myretpol1", None),
          CreateResourceReq("myres2", "myrestype2", "myretpol2", None)
        ),
        Seq(
          UploadFileReq("myres1", "myfile1", "mime1", Array.emptyByteArray),
          UploadFileReq("myres2", "myfile2", "mime2", Array.emptyByteArray),
          UploadFileReq("myres2", "myfile3", "mime3", Array.emptyByteArray)
        ),
        Job.spark("y", "y", "z", "cn", None, None, None, None, None, None, None, None, None, None)
      )
    )

    assert(actual.isLeft)
    assert(actual.left.value.error.startsWith("GetJobErr(GetJobReq"))
  }

  test("undeploy return Left(ComponentGatewayError) on get job some") {
    inSequence(
      (cdeClientFactory.create _)
        .expects("my-dp-domain", "my-dp-name")
        .once()
        .returns(Right(cdeClusterClient)),
      (cdeClusterClient.safeDeleteJob _)
        .expects(
          CdeRequest(
            srvDesc,
            vcDesc,
            DeleteJobReq("y")
          )
        )
        .returns(Right()),
      (cdeClusterClient.safeDeleteResource _)
        .expects(CdeRequest(srvDesc, vcDesc, DeleteResourceReq("myres1")))
        .returns(Right()),
      (cdeClusterClient.safeDeleteResource _)
        .expects(CdeRequest(srvDesc, vcDesc, DeleteResourceReq("myres2")))
        .returns(Right()),
      (cdeClusterClient.getJob _)
        .expects(CdeRequest(srvDesc, vcDesc, GetJobReq("y")))
        .returns(
          Right(
            GetJobRes(
              Some(
                JobDetails(
                  "name",
                  "type",
                  "created",
                  "modified",
                  "lastUsed",
                  Seq.empty,
                  "retentionPolicy",
                  None,
                  None
                )
              )
            )
          )
        )
    )

    val actual = sparkJobDeployer.undeployJob(
      gateway.workload.SparkCdeWorkload(
        "my-dp-domain",
        "my-dp-name",
        srvDesc,
        vcDesc,
        Seq(
          CreateResourceReq("myres1", "myrestype1", "myretpol1", None),
          CreateResourceReq("myres2", "myrestype2", "myretpol2", None)
        ),
        Seq(
          UploadFileReq("myres1", "myfile1", "mime1", Array.emptyByteArray),
          UploadFileReq("myres2", "myfile2", "mime2", Array.emptyByteArray),
          UploadFileReq("myres2", "myfile3", "mime3", Array.emptyByteArray)
        ),
        Job.spark("y", "y", "z", "cn", None, None, None, None, None, None, None, None, None, None)
      )
    )

    assert(actual.isLeft)
    assert(actual.left.value.error == "Job was properly destroyed but get job operation still return it")
  }

}

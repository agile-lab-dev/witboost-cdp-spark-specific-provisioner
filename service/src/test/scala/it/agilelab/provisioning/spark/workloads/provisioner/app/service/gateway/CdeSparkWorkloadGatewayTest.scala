package it.agilelab.provisioning.spark.workloads.provisioner.app.service.gateway

import com.cloudera.cdp.de.model.{ ServiceDescription, VcDescription }
import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.base.{ Job, JobDetails, Mount, SparkJob }
import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.request.{ CreateResourceReq, UploadFileReq }
import it.agilelab.provisioning.mesh.self.service.api.model.Component.Workload
import it.agilelab.provisioning.mesh.self.service.api.model.{ DataProduct, ProvisionRequest }
import it.agilelab.provisioning.mesh.self.service.core.gateway.ComponentGatewayError
import it.agilelab.provisioning.mesh.self.service.core.model.ProvisionCommand
import it.agilelab.provisioning.spark.workload.core.models.DpCdp
import it.agilelab.provisioning.spark.workload.core.{ SparkCde, SparkWorkloadResponse }
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.mapper.SparkCdeWorkloadMapper
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.workload.{
  SparkCdeWorkload,
  SparkCdeWorkloadGateway
}
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.{ workload, CdeSparkWorkloadGateway }
import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite

class CdeSparkWorkloadGatewayTest extends AnyFunSuite with MockFactory {
  val sparkCdeWorkloadGateway: SparkCdeWorkloadGateway = mock[SparkCdeWorkloadGateway]
  val sparkCdeWorkloadMapper: SparkCdeWorkloadMapper   = mock[SparkCdeWorkloadMapper]

  val sparkWorkloadGateway = new CdeSparkWorkloadGateway(
    sparkCdeWorkloadMapper,
    sparkCdeWorkloadGateway
  )

  val serviceDesc = new ServiceDescription()
  val vcDesc      = new VcDescription()

  val provisionCommand: ProvisionCommand[DpCdp, SparkCde] = ProvisionCommand(
    requestId = "x",
    provisionRequest = ProvisionRequest(
      dataProduct = DataProduct(
        id = "my-dp-id",
        name = "my-dp-name",
        domain = "my-dp-domain",
        environment = "dev",
        version = "1",
        dataProductOwner = "dpOwner",
        devGroup = "dev-group",
        ownerGroup = "owner-group",
        specific = new DpCdp,
        components = Seq()
      ),
      component = Some(
        Workload(
          id = "my-dp-wl-id",
          name = "my-dp-name",
          description = "my-dp-desc",
          version = "0.0.1",
          specific = SparkCde.SparkCdeJob(
            cdeService = "my-cde-service",
            cdeCluster = "my-cde-cluster",
            jobName = "my-job-name",
            jar = "s3://bucket/jarfile.jar",
            className = "com.MyClass",
            jobConfig = None
          )
        )
      )
    )
  )

  test("create return Right(SparkWorkloadGateway)") {
    inSequence(
      (sparkCdeWorkloadMapper.map _)
        .expects(*, *)
        .returns(
          Right(
            SparkCdeWorkload(
              "my-dp-domain",
              "my-dp-name",
              serviceDesc,
              vcDesc,
              Seq(CreateResourceReq("x", "y", "z", None)),
              Seq(UploadFileReq("r", "f", "m", Array.emptyByteArray)),
              Job.spark("y", "y", "z", "cn", None, None, None, None, None, None, None, None, None, None)
            )
          )
        ),
      (sparkCdeWorkloadGateway.deployJob _)
        .expects(
          SparkCdeWorkload(
            "my-dp-domain",
            "my-dp-name",
            serviceDesc,
            vcDesc,
            Seq(CreateResourceReq("x", "y", "z", None)),
            Seq(UploadFileReq("r", "f", "m", Array.emptyByteArray)),
            Job.spark("y", "y", "z", "cn", None, None, None, None, None, None, None, None, None, None)
          )
        )
        .returns(
          Right(
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
        )
    )

    val actual   = sparkWorkloadGateway.create(provisionCommand)
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

  test("create return Left(SparkWorkloadGateway) on spark deploy error") {
    inSequence(
      (sparkCdeWorkloadMapper.map _)
        .expects(*, *)
        .returns(
          Right(
            SparkCdeWorkload(
              "my-dp-domain",
              "my-dp-name",
              serviceDesc,
              vcDesc,
              Seq(CreateResourceReq("x", "y", "z", None)),
              Seq(UploadFileReq("r", "f", "m", Array.emptyByteArray)),
              Job.spark("y", "y", "z", "cn", None, None, None, None, None, None, None, None, None, None)
            )
          )
        ),
      (sparkCdeWorkloadGateway.deployJob _)
        .expects(
          workload.SparkCdeWorkload(
            "my-dp-domain",
            "my-dp-name",
            serviceDesc,
            vcDesc,
            Seq(CreateResourceReq("x", "y", "z", None)),
            Seq(UploadFileReq("r", "f", "m", Array.emptyByteArray)),
            Job.spark("y", "y", "z", "cn", None, None, None, None, None, None, None, None, None, None)
          )
        )
        .returns(Left(ComponentGatewayError("y")))
    )

    val actual   = sparkWorkloadGateway.create(provisionCommand)
    val expected = Left(ComponentGatewayError("y"))
    assert(actual == expected)
  }

  test("create return Left(ComponentGatewayError) on mapLogic error") {
    (sparkCdeWorkloadMapper.map _)
      .expects(*, *)
      .returns(Left(ComponentGatewayError("z")))

    val actual   = sparkWorkloadGateway.create(provisionCommand)
    val expected = Left(ComponentGatewayError("z"))
    assert(actual == expected)
  }

  test("destroy return Right(SparkWorkloadGateway)") {
    inSequence(
      (sparkCdeWorkloadMapper.map _)
        .expects(*, *)
        .returns(
          Right(
            SparkCdeWorkload(
              "my-dp-domain",
              "my-dp-name",
              serviceDesc,
              vcDesc,
              Seq(CreateResourceReq("x", "y", "z", None)),
              Seq(UploadFileReq("r", "f", "m", Array.emptyByteArray)),
              Job.spark("y", "y", "z", "cn", None, None, None, None, None, None, None, None, None, None)
            )
          )
        ),
      (sparkCdeWorkloadGateway.undeployJob _)
        .expects(
          workload.SparkCdeWorkload(
            "my-dp-domain",
            "my-dp-name",
            serviceDesc,
            vcDesc,
            Seq(CreateResourceReq("x", "y", "z", None)),
            Seq(UploadFileReq("r", "f", "m", Array.emptyByteArray)),
            Job.spark("y", "y", "z", "cn", None, None, None, None, None, None, None, None, None, None)
          )
        )
        .returns(
          Right(
            SparkWorkloadResponse(
              None,
              Some(
                Job(
                  name = "jobname",
                  `type` = "spark",
                  mounts = Seq(Mount("jobres")),
                  retentionPolicy = "keep_indefinitely",
                  spark = Some(
                    SparkJob.defaultSparkJob(
                      file = "workloadJarKey.jar",
                      className = "it.agilelab.classPath.MyClass"
                    )
                  ),
                  airflow = None,
                  schedule = None
                )
              )
            )
          )
        )
    )

    val actual   = sparkWorkloadGateway.destroy(provisionCommand)
    val expected = Right(
      SparkWorkloadResponse(
        None,
        Some(
          Job(
            name = "jobname",
            `type` = "spark",
            mounts = Seq(Mount("jobres")),
            retentionPolicy = "keep_indefinitely",
            spark = Some(
              SparkJob.defaultSparkJob(
                file = "workloadJarKey.jar",
                className = "it.agilelab.classPath.MyClass"
              )
            ),
            airflow = None,
            schedule = None
          )
        )
      )
    )
    assert(actual == expected)
  }

  test("destroy return Left(SparkWorkloadGateway) on spark undeploy error") {
    inSequence(
      (sparkCdeWorkloadMapper.map _)
        .expects(*, *)
        .returns(
          Right(
            workload.SparkCdeWorkload(
              "my-dp-domain",
              "my-dp-name",
              serviceDesc,
              vcDesc,
              Seq(CreateResourceReq("x", "y", "z", None)),
              Seq(UploadFileReq("r", "f", "m", Array.emptyByteArray)),
              Job.spark("y", "y", "z", "cn", None, None, None, None, None, None, None, None, None, None)
            )
          )
        ),
      (sparkCdeWorkloadGateway.undeployJob _)
        .expects(
          workload.SparkCdeWorkload(
            "my-dp-domain",
            "my-dp-name",
            serviceDesc,
            vcDesc,
            Seq(CreateResourceReq("x", "y", "z", None)),
            Seq(UploadFileReq("r", "f", "m", Array.emptyByteArray)),
            Job.spark("y", "y", "z", "cn", None, None, None, None, None, None, None, None, None, None)
          )
        )
        .returns(Left(ComponentGatewayError("y")))
    )

    val actual   = sparkWorkloadGateway.destroy(provisionCommand)
    val expected = Left(ComponentGatewayError("y"))
    assert(actual == expected)
  }

  test("destroy return Left(ComponentGatewayError) on mapLogic error") {
    (sparkCdeWorkloadMapper.map _)
      .expects(*, *)
      .returns(Left(ComponentGatewayError("z")))

    val actual   = sparkWorkloadGateway.destroy(provisionCommand)
    val expected = Left(ComponentGatewayError("z"))
    assert(actual == expected)
  }

}

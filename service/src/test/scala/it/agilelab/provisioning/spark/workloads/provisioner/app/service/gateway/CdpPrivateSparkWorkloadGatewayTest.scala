package it.agilelab.provisioning.spark.workloads.provisioner.app.service.gateway

import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.base.{ Job, JobDetails, Mount, SparkJob }
import it.agilelab.provisioning.mesh.self.service.api.model.Component.Workload
import it.agilelab.provisioning.mesh.self.service.api.model.{ DataProduct, ProvisionRequest }
import it.agilelab.provisioning.mesh.self.service.core.gateway.ComponentGatewayError
import it.agilelab.provisioning.mesh.self.service.core.model.ProvisionCommand
import it.agilelab.provisioning.spark.workloads.core.{ SparkCdpPrivate, SparkWorkloadResponse }
import it.agilelab.provisioning.spark.workloads.core.SparkCdpPrivate._
import it.agilelab.provisioning.spark.workloads.core.models.DpCdp
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.workload.{
  SparkCdeWorkload,
  SparkCdpPrivateWorkload,
  SparkCdpPrivateWorkloadGateway
}
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.{ workload, CdpPrivateSparkWorkloadGateway }
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.mapper.SparkCdpPrivateWorkloadMapper
import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite

class CdpPrivateSparkWorkloadGatewayTest extends AnyFunSuite with MockFactory {
  val sparkCdpPrivateWorkloadGateway: SparkCdpPrivateWorkloadGateway = mock[SparkCdpPrivateWorkloadGateway]
  val sparkCdpPrivateWorkloadMapper: SparkCdpPrivateWorkloadMapper   = mock[SparkCdpPrivateWorkloadMapper]

  val sparkWorkloadGateway = new CdpPrivateSparkWorkloadGateway(
    sparkCdpPrivateWorkloadMapper,
    sparkCdpPrivateWorkloadGateway
  )

  val provisionCommand: ProvisionCommand[DpCdp, SparkCdpPrivate] = ProvisionCommand(
    requestId = "x",
    provisionRequest = ProvisionRequest(
      dataProduct = DataProduct(
        id = "urn:dmb:cmp:my-dp-id",
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
        Workload[SparkCdpPrivate](
          id = "urn:dmb:cmp:domain:my-dp-id:1:wl-id",
          name = "my-dp-name",
          description = "my-dp-desc",
          version = "0.0.1",
          specific = SparkCdpPrivateJob(
            jobName = "my-job-name",
            jar = "folder://folder/jarfile.jar",
            className = "com.MyClass",
            jobConfig = None,
            queue = ""
          )
        )
      )
    )
  )

  test("create return Right(SparkWorkloadGateway)") {
    inSequence(
      (sparkCdpPrivateWorkloadMapper.map _)
        .expects(*, *)
        .returns(
          Right(
            SparkCdpPrivateWorkload(
              "my-dp-domain",
              "my-dp-name",
              "default",
              Job.spark("y", "y", "z", "cn", None, None, None, None, None, None, None, None, None, None)
            )
          )
        ),
      (sparkCdpPrivateWorkloadGateway.deployJob _)
        .expects(
          SparkCdpPrivateWorkload(
            "my-dp-domain",
            "my-dp-name",
            "default",
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
      (sparkCdpPrivateWorkloadMapper.map _)
        .expects(*, *)
        .returns(
          Right(
            SparkCdpPrivateWorkload(
              "my-dp-domain",
              "my-dp-name",
              "default",
              Job.spark("y", "y", "z", "cn", None, None, None, None, None, None, None, None, None, None)
            )
          )
        ),
      (sparkCdpPrivateWorkloadGateway.deployJob _)
        .expects(
          workload.SparkCdpPrivateWorkload(
            "my-dp-domain",
            "my-dp-name",
            "default",
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
    (sparkCdpPrivateWorkloadMapper.map _)
      .expects(*, *)
      .returns(Left(ComponentGatewayError("z")))

    val actual   = sparkWorkloadGateway.create(provisionCommand)
    val expected = Left(ComponentGatewayError("z"))
    assert(actual == expected)
  }

  test("destroy return Right(SparkWorkloadGateway)") {
    inSequence(
      (sparkCdpPrivateWorkloadMapper.map _)
        .expects(*, *)
        .returns(
          Right(
            SparkCdpPrivateWorkload(
              "my-dp-domain",
              "my-dp-name",
              "default",
              Job.spark("y", "y", "z", "cn", None, None, None, None, None, None, None, None, None, None)
            )
          )
        ),
      (sparkCdpPrivateWorkloadGateway.undeployJob _)
        .expects(
          workload.SparkCdpPrivateWorkload(
            "my-dp-domain",
            "my-dp-name",
            "default",
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
      (sparkCdpPrivateWorkloadMapper.map _)
        .expects(*, *)
        .returns(
          Right(
            SparkCdpPrivateWorkload(
              "my-dp-domain",
              "my-dp-name",
              "default",
              Job.spark("y", "y", "z", "cn", None, None, None, None, None, None, None, None, None, None)
            )
          )
        ),
      (sparkCdpPrivateWorkloadGateway.undeployJob _)
        .expects(
          workload.SparkCdpPrivateWorkload(
            "my-dp-domain",
            "my-dp-name",
            "default",
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
    (sparkCdpPrivateWorkloadMapper.map _)
      .expects(*, *)
      .returns(Left(ComponentGatewayError("z")))

    val actual   = sparkWorkloadGateway.destroy(provisionCommand)
    val expected = Left(ComponentGatewayError("z"))
    assert(actual == expected)
  }

}

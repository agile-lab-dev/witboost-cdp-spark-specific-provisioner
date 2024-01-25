package it.agilelab.provisioning.spark.workloads.provisioner.app.service.gateway

import it.agilelab.provisioning.commons.audit.Audit
import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.base.{ Job, JobDetails, Mount, SparkJob }
import it.agilelab.provisioning.mesh.self.service.api.model.Component.Workload
import it.agilelab.provisioning.mesh.self.service.api.model.{ DataProduct, ProvisionRequest }
import it.agilelab.provisioning.mesh.self.service.core.gateway.ComponentGatewayError
import it.agilelab.provisioning.mesh.self.service.core.model.ProvisionCommand
import it.agilelab.provisioning.spark.workload.core.models.DpCdp
import it.agilelab.provisioning.spark.workload.core.{ SparkCde, SparkWorkloadResponse }
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.{
  CdeSparkWorkloadGateway,
  CdeSparkWorkloadGatewayWithAudit
}
import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite

class CdeSparkWorkloadGatewayWithAuditTest extends AnyFunSuite with MockFactory {

  val baseSparkWorkloadGateway: CdeSparkWorkloadGateway = mock[CdeSparkWorkloadGateway]
  val audit: Audit                                      = mock[Audit]

  val sparkWorkloadGateway = new CdeSparkWorkloadGatewayWithAudit(
    baseSparkWorkloadGateway,
    audit
  )

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

  test("create logs info on success") {
    inSequence(
      (audit.info _)
        .expects(
          "Executing Create(ProvisionCommand(x,ProvisionRequest(DataProduct(my-dp-id,my-dp-name,my-dp-domain,dev,1,dpOwner,dev-group,owner-group,DpCdp(),List()),Some(Workload(my-dp-wl-id,my-dp-name,my-dp-desc,0.0.1,SparkCdeJob(my-cde-service,my-cde-cluster,my-job-name,s3://bucket/jarfile.jar,com.MyClass,None))))))"
        )
        .once(),
      (baseSparkWorkloadGateway.create _)
        .expects(provisionCommand)
        .once()
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
        ),
      (audit.info _)
        .expects("SparkWorkload successfully created.")
        .once()
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

  test("create logs error on failure") {
    inSequence(
      (audit.info _)
        .expects(
          "Executing Create(ProvisionCommand(x,ProvisionRequest(DataProduct(my-dp-id,my-dp-name,my-dp-domain,dev,1,dpOwner,dev-group,owner-group,DpCdp(),List()),Some(Workload(my-dp-wl-id,my-dp-name,my-dp-desc,0.0.1,SparkCdeJob(my-cde-service,my-cde-cluster,my-job-name,s3://bucket/jarfile.jar,com.MyClass,None))))))"
        )
        .once(),
      (baseSparkWorkloadGateway.create _)
        .expects(provisionCommand)
        .once()
        .returns(Left(ComponentGatewayError("z"))),
      (audit.error _)
        .expects(
          "SparkWorkload creation failed. Details: z"
        )
        .once()
    )

    val expected = Left(ComponentGatewayError("z"))
    val actual   = sparkWorkloadGateway.create(provisionCommand)
    assert(actual == expected)
  }

  test("destroy logs info on success") {
    inSequence(
      (audit.info _)
        .expects(
          "Executing Destroy(ProvisionCommand(x,ProvisionRequest(DataProduct(my-dp-id,my-dp-name,my-dp-domain,dev,1,dpOwner,dev-group,owner-group,DpCdp(),List()),Some(Workload(my-dp-wl-id,my-dp-name,my-dp-desc,0.0.1,SparkCdeJob(my-cde-service,my-cde-cluster,my-job-name,s3://bucket/jarfile.jar,com.MyClass,None))))))"
        )
        .once(),
      (baseSparkWorkloadGateway.destroy _)
        .expects(provisionCommand)
        .once()
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
        ),
      (audit.info _)
        .expects("SparkWorkload successfully destroyed.")
        .once()
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

  test("destroy logs error on failure") {
    inSequence(
      (audit.info _)
        .expects(
          "Executing Destroy(ProvisionCommand(x,ProvisionRequest(DataProduct(my-dp-id,my-dp-name,my-dp-domain,dev,1,dpOwner,dev-group,owner-group,DpCdp(),List()),Some(Workload(my-dp-wl-id,my-dp-name,my-dp-desc,0.0.1,SparkCdeJob(my-cde-service,my-cde-cluster,my-job-name,s3://bucket/jarfile.jar,com.MyClass,None))))))"
        )
        .once(),
      (baseSparkWorkloadGateway.destroy _)
        .expects(provisionCommand)
        .once()
        .returns(Left(ComponentGatewayError("z"))),
      (audit.error _)
        .expects(
          "SparkWorkload destroy failed. Details: z"
        )
        .once()
    )

    val expected = Left(ComponentGatewayError("z"))
    val actual   = sparkWorkloadGateway.destroy(provisionCommand)
    assert(actual == expected)
  }

}

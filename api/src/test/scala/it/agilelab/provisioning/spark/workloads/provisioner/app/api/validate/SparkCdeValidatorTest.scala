package it.agilelab.provisioning.spark.workloads.provisioner.app.api.validate

import cats.data.Validated.{ invalidNel, valid }
import cats.data.{ NonEmptyList, Validated }
import com.cloudera.cdp.de.model.{ ServiceSummary, VcSummary }
import it.agilelab.provisioning.aws.s3.gateway.S3Gateway
import it.agilelab.provisioning.commons.client.cdp.de.CdpDeClient
import it.agilelab.provisioning.commons.validator.ValidationFail
import it.agilelab.provisioning.mesh.self.service.api.model.Component._
import it.agilelab.provisioning.mesh.self.service.api.model.{ DataProduct, ProvisionRequest }
import it.agilelab.provisioning.spark.workloads.core.{ JobConfig, SparkCde }
import it.agilelab.provisioning.spark.workloads.core.SparkCde._
import it.agilelab.provisioning.spark.workloads.core.models.DpCdp
import it.agilelab.provisioning.spark.workloads.provisioner.app.api.validate
import it.agilelab.provisioning.spark.workloads.provisioner.app.api.validate.CdeValidationErrors
import it.agilelab.provisioning.spark.workloads.provisioner.app.api.validate.SparkCdeValidator.validator
import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite

class SparkCdeValidatorTest extends AnyFunSuite with MockFactory {
  val s3Gateway: S3Gateway     =
    mock[S3Gateway]
  val cdpDeClient: CdpDeClient =
    mock[CdpDeClient]

  private val specificSpark: SparkCdeJob   = SparkCdeJob(
    cdeService = "my-dp-cde-service",
    cdeCluster = "my-dp-cde-cluster",
    jobName = "my-dm-my-dp-1-my-wl-my-dp-environment",
    jar = "s3://bucket/my-jar",
    className = "my-class",
    jobConfig = None
  )
  private val workload: Workload[SparkCde] = Workload[SparkCde](
    id = "urn:dmb:cmp:my_dm.my_dp.1.my_wl",
    name = "my-dp-wl-name",
    description = "my-dp-desc",
    version = "my-dp-version",
    specific = specificSpark
  )

  private val dataProduct: DataProduct[DpCdp] = DataProduct[DpCdp](
    id = "my-dp-id",
    name = "my-dp-name",
    domain = "my-dp-domain",
    environment = "my-dp-environment",
    version = "my-dp-version",
    dataProductOwner = "my-dp-owner",
    devGroup = "dev-group",
    ownerGroup = "owner-group",
    specific = new DpCdp,
    components = Seq()
  )

  test("validate return valid with basic workload") {
    val serviceSummary: ServiceSummary = new ServiceSummary()
    serviceSummary.setClusterId("x")
    serviceSummary.setStatus("ClusterCreationCompleted")
    val vcSummary: VcSummary           = new VcSummary()
    vcSummary.setStatus("AppInstalled")

    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findVcByName _)
      .expects(*, *)
      .returns(Right(Some(vcSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findVcByName _)
      .expects(*, *)
      .returns(Right(Some(vcSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findVcByName _)
      .expects(*, *)
      .returns(Right(Some(vcSummary)))
    (s3Gateway.objectExists _)
      .expects(*, *)
      .returns(Right(true))

    val actual = validator(
      cdpDeClient,
      s3Gateway
    ).validate(ProvisionRequest(dataProduct, Some(workload)))
    assert(actual == Right(valid(ProvisionRequest(dataProduct, Some(workload)))))
  }

  test("validate return invalid with CDE Service not found") {
    val serviceSummary: ServiceSummary = new ServiceSummary()
    serviceSummary.setClusterId("x")
    val vcSummary: VcSummary           = new VcSummary()
    vcSummary.setStatus("AppInstalled")

    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(None))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(None))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(None))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findVcByName _)
      .expects(*, *)
      .returns(Right(Some(vcSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findVcByName _)
      .expects(*, *)
      .returns(Right(Some(vcSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findVcByName _)
      .expects(*, *)
      .returns(Right(Some(vcSummary)))
    (s3Gateway.objectExists _)
      .expects(*, *)
      .returns(Right(true))

    val actual = validator(
      cdpDeClient,
      s3Gateway
    ).validate(ProvisionRequest(dataProduct, Some(workload)))
    assert(
      actual == Right(
        invalidNel(
          ValidationFail(ProvisionRequest(dataProduct, Some(workload)), CdeValidationErrors.CDE_SERVICE_NOT_FOUND)
        )
      )
    )
  }

  test("validate return invalid with CDE Service not running") {
    val serviceSummary: ServiceSummary = new ServiceSummary()
    serviceSummary.setClusterId("x")
    serviceSummary.setStatus("NotValidStatus")
    val vcSummary: VcSummary           = new VcSummary()
    vcSummary.setStatus("AppInstalled")

    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findVcByName _)
      .expects(*, *)
      .returns(Right(Some(vcSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findVcByName _)
      .expects(*, *)
      .returns(Right(Some(vcSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findVcByName _)
      .expects(*, *)
      .returns(Right(Some(vcSummary)))
    (s3Gateway.objectExists _)
      .expects(*, *)
      .returns(Right(true))

    val actual = validator(
      cdpDeClient,
      s3Gateway
    ).validate(ProvisionRequest(dataProduct, Some(workload)))

    assert(
      actual == Right(
        invalidNel(
          ValidationFail(ProvisionRequest(dataProduct, Some(workload)), CdeValidationErrors.CDE_SERVICE_NOT_RUNNING)
        )
      )
    )
  }

  test("validate return invalid with CDE Service recently deleted") {
    val serviceSummary: ServiceSummary = new ServiceSummary()
    serviceSummary.setClusterId("x")
    serviceSummary.setStatus("ClusterDeletionCompleted")
    val vcSummary: VcSummary           = new VcSummary()
    vcSummary.setStatus("AppInstalled")

    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findVcByName _)
      .expects(*, *)
      .returns(Right(Some(vcSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findVcByName _)
      .expects(*, *)
      .returns(Right(Some(vcSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findVcByName _)
      .expects(*, *)
      .returns(Right(Some(vcSummary)))
    (s3Gateway.objectExists _)
      .expects(*, *)
      .returns(Right(true))

    val actual = validator(
      cdpDeClient,
      s3Gateway
    ).validate(ProvisionRequest(dataProduct, Some(workload)))

    assert(
      actual == Right(
        invalidNel(
          ValidationFail(ProvisionRequest(dataProduct, Some(workload)), CdeValidationErrors.CDE_SERVICE_DELETED)
        )
      )
    )

  }

  test("validate return invalid with CDE Virtual Cluster not found") {
    val serviceSummary: ServiceSummary = new ServiceSummary()
    serviceSummary.setClusterId("x")
    val vcSummary: VcSummary           = new VcSummary()
    vcSummary.setStatus("AppInstalled")

    //Testing the CDE service
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(None))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    //Testing the CDE virtual cluster
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findVcByName _)
      .expects(*, *)
      .returns(Right(None))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findVcByName _)
      .expects(*, *)
      .returns(Right(None))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findVcByName _)
      .expects(*, *)
      .returns(Right(None))
    //Testing the job
    (s3Gateway.objectExists _)
      .expects(*, *)
      .returns(Right(true))

    val actual = validator(
      cdpDeClient,
      s3Gateway
    ).validate(ProvisionRequest(dataProduct, Some(workload)))
    assert(
      actual == Right(
        invalidNel(
          ValidationFail(
            ProvisionRequest(dataProduct, Some(workload)),
            CdeValidationErrors.CDE_VIRTUAL_CLUSTER_NOT_FOUND
          )
        )
      )
    )
  }

  test("validate return invalid with CDE cluster not properly activated") {
    val serviceSummary: ServiceSummary = new ServiceSummary()
    serviceSummary.setClusterId("x")
    serviceSummary.setStatus("ClusterCreationCompleted")
    val vcSummary: VcSummary           = new VcSummary()
    vcSummary.setStatus("NotValidStatus")

    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findVcByName _)
      .expects(*, *)
      .returns(Right(Some(vcSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findVcByName _)
      .expects(*, *)
      .returns(Right(Some(vcSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findVcByName _)
      .expects(*, *)
      .returns(Right(Some(vcSummary)))
    (s3Gateway.objectExists _)
      .expects(*, *)
      .returns(Right(true))

    val actual = validator(
      cdpDeClient,
      s3Gateway
    ).validate(ProvisionRequest(dataProduct, Some(workload)))
    assert(
      actual == Right(
        invalidNel(
          ValidationFail(
            ProvisionRequest(dataProduct, Some(workload)),
            CdeValidationErrors.CDE_VIRTUAL_CLUSTER_NOT_ACTIVATED
          )
        )
      )
    )
  }

  test("validate return invalid with CDE cluster recently deleted") {
    val serviceSummary: ServiceSummary = new ServiceSummary()
    serviceSummary.setClusterId("x")
    serviceSummary.setStatus("ClusterCreationCompleted")
    val vcSummary: VcSummary           = new VcSummary()
    vcSummary.setStatus("AppDeleted")

    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findVcByName _)
      .expects(*, *)
      .returns(Right(Some(vcSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findVcByName _)
      .expects(*, *)
      .returns(Right(Some(vcSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findVcByName _)
      .expects(*, *)
      .returns(Right(Some(vcSummary)))
    (s3Gateway.objectExists _)
      .expects(*, *)
      .returns(Right(true))

    val actual = validator(
      cdpDeClient,
      s3Gateway
    ).validate(ProvisionRequest(dataProduct, Some(workload)))
    assert(
      actual == Right(
        invalidNel(
          ValidationFail(ProvisionRequest(dataProduct, Some(workload)), CdeValidationErrors.CDE_VIRTUAL_CLUSTER_DELETED)
        )
      )
    )
  }

  test("validate return invalid with bad jobname") {

    val serviceSummary: ServiceSummary = new ServiceSummary()
    serviceSummary.setClusterId("x")
    serviceSummary.setStatus("ClusterCreationCompleted")
    val vcSummary: VcSummary           = new VcSummary()
    vcSummary.setStatus("AppInstalled")

    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findVcByName _)
      .expects(*, *)
      .returns(Right(Some(vcSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findVcByName _)
      .expects(*, *)
      .returns(Right(Some(vcSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findVcByName _)
      .expects(*, *)
      .returns(Right(Some(vcSummary)))
    (s3Gateway.objectExists _)
      .expects(*, *)
      .returns(Right(true))

    val s: SparkCde = specificSpark.copy(jobName = "jn")
    val wl          = workload.copy(specific = s)
    val actual      = validator(cdpDeClient, s3Gateway).validate(ProvisionRequest(dataProduct, Some(wl)))
    assert(actual == Right(invalidNel(ValidationFail(ProvisionRequest(dataProduct, Some(wl)), "Job name not valid"))))
  }

  test("validate return invalid with wrong config") {
    val serviceSummary: ServiceSummary = new ServiceSummary()
    serviceSummary.setClusterId("x")
    serviceSummary.setStatus("ClusterCreationCompleted")
    val vcSummary: VcSummary           = new VcSummary()
    vcSummary.setStatus("AppInstalled")

    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findVcByName _)
      .expects(*, *)
      .returns(Right(Some(vcSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findVcByName _)
      .expects(*, *)
      .returns(Right(Some(vcSummary)))
    (cdpDeClient.findServiceByName _)
      .expects(*)
      .returns(Right(Some(serviceSummary)))
    (cdpDeClient.findVcByName _)
      .expects(*, *)
      .returns(Right(Some(vcSummary)))
    (s3Gateway.objectExists _)
      .expects(*, *)
      .returns(Right(false))

    val s: SparkCde = specificSpark.copy(jobConfig =
      Some(
        JobConfig(
          args = None,
          dependencies = None,
          driverCores = Some(0),
          driverMemory = Some("1m"),
          executorCores = Some(0),
          executorMemory = Some("1m"),
          numExecutors = Some(0),
          logLevel = None,
          conf = None,
          schedule = None
        )
      )
    )
    val wl          = workload.copy(specific = s)
    val actual      = validator(cdpDeClient, s3Gateway).validate(ProvisionRequest(dataProduct, Some(wl)))
    val expected    = Right(
      Validated.invalid(
        NonEmptyList(
          ValidationFail(ProvisionRequest(dataProduct, Some(wl)), "Job Source application file not found"),
          List(
            ValidationFail(
              ProvisionRequest(dataProduct, Some(wl)),
              "If specified, driver cores must be a positive integer"
            ),
            ValidationFail(
              ProvisionRequest(dataProduct, Some(wl)),
              "If specified, driver memory must be a positive integer followed by 'g' (e.g. 2g)"
            ),
            ValidationFail(
              ProvisionRequest(dataProduct, Some(wl)),
              "If specified, executor cores must be a positive integer"
            ),
            ValidationFail(
              ProvisionRequest(dataProduct, Some(wl)),
              "If specified, executor memory must be a positive integer followed by 'g' (e.g. 2g)"
            ),
            ValidationFail(
              ProvisionRequest(dataProduct, Some(wl)),
              "If specified, num executors must be a positive integer"
            )
          )
        )
      )
    )
    assert(actual == expected)
  }

}

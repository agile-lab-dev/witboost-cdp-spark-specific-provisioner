package it.agilelab.provisioning.spark.workloads.provisioner.app.api.validate

import cats.data.Validated.{ invalidNel, valid }
import cats.data.{ NonEmptyList, Validated }
import it.agilelab.provisioning.commons.validator.ValidationFail
import it.agilelab.provisioning.mesh.self.service.api.model.Component._
import it.agilelab.provisioning.mesh.self.service.api.model.{ DataProduct, ProvisionRequest }
import it.agilelab.provisioning.spark.workload.core.SparkCdpPrivate._
import it.agilelab.provisioning.spark.workload.core.models.DpCdp
import it.agilelab.provisioning.spark.workload.core.{ JobConfig, SparkCdpPrivate }
import it.agilelab.provisioning.spark.workloads.provisioner.app.api.validate.SparkCdpPrivateValidator.validator
import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite

class SparkCdpPrivateValidatorTest extends AnyFunSuite with MockFactory {

  private val specificSpark: SparkCdpPrivateJob   = SparkCdpPrivateJob(
    jobName = "my-dm-my-dp-1-my-wl-my-dp-environment",
    jar = "folder://folder/my-jar",
    className = "my-class",
    jobConfig = None
  )
  private val workload: Workload[SparkCdpPrivate] = Workload[SparkCdpPrivate](
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
    val actual = validator().validate(ProvisionRequest(dataProduct, Some(workload)))
    assert(actual == Right(valid(ProvisionRequest(dataProduct, Some(workload)))))
  }

  test("validate return invalid with bad jobname") {
    val s: SparkCdpPrivate = specificSpark.copy(jobName = "jn")
    val wl                 = workload.copy(specific = s)
    val actual             = validator().validate(ProvisionRequest(dataProduct, Some(wl)))
    assert(actual == Right(invalidNel(ValidationFail(ProvisionRequest(dataProduct, Some(wl)), "Job name not valid"))))
  }

  test("validate return invalid with wrong config") {

    val invalidJobConfig = JobConfig(
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

    val s: SparkCdpPrivate = specificSpark.copy(jobConfig = Some(invalidJobConfig))
    val wl                 = workload.copy(specific = s)
    val actual             = validator().validate(ProvisionRequest(dataProduct, Some(wl)))

    val expectedErrors = NonEmptyList
      .of(
        "If specified, driver cores must be a positive integer",
        "If specified, driver memory must be a positive integer followed by 'g' (e.g. 2g)",
        "If specified, executor cores must be a positive integer",
        "If specified, executor memory must be a positive integer followed by 'g' (e.g. 2g)",
        "If specified, num executors must be a positive integer"
      )
      .map(msg => ValidationFail(ProvisionRequest(dataProduct, Some(wl)), msg))

    val expected = Right(Validated.invalid(expectedErrors))

    assert(actual == expected)
  }

}

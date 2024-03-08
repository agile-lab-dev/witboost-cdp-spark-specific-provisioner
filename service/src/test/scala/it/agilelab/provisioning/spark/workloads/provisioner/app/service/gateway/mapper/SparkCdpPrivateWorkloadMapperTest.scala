package it.agilelab.provisioning.spark.workloads.provisioner.app.service.gateway.mapper

import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.base.{ Job, Schedule }
import it.agilelab.provisioning.mesh.self.service.api.model.DataProduct
import it.agilelab.provisioning.mesh.self.service.api.model.Component.Workload
import it.agilelab.provisioning.spark.workloads.core.{ JobConfig, JobScheduler, SparkCdpPrivate }
import it.agilelab.provisioning.spark.workloads.core.SparkCdpPrivate.SparkCdpPrivateJob
import it.agilelab.provisioning.spark.workloads.core.models.DpCdp
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.mapper.SparkCdpPrivateWorkloadMapper
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.workload.SparkCdpPrivateWorkload
import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite

class SparkCdpPrivateWorkloadMapperTest extends AnyFunSuite with MockFactory {

  private val sparkCdpPrivateJobMapper: SparkCdpPrivateWorkloadMapper = new SparkCdpPrivateWorkloadMapper()

  test("map spark workload") {

    val dataProduct = DataProduct(
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
    )

    val workload = Workload[SparkCdpPrivate](
      id = "urn:dmb:cmp:domain:my-dp-id:1:wl-id",
      name = "my-dp-name",
      description = "my-dp-desc",
      version = "0.0.1",
      specific = SparkCdpPrivateJob(
        jobName = "my-job-name",
        jar = "hdfs://localhost:8020/jarfile.jar",
        className = "com.MyClass",
        jobConfig = None,
        queue = "default"
      )
    )

    val actual = sparkCdpPrivateJobMapper.map(dataProduct, workload)

    val expected = Right(
      SparkCdpPrivateWorkload(
        domain = "my-dp-domain",
        dataProduct = "my-dp-name",
        queue = "default",
        job = Job.spark(
          name = "my-job-name",
          resource = "mainres-7f59fed658cdded64d35590c3c86003d",
          filePath = "hdfs://localhost:8020/jarfile.jar",
          className = "com.MyClass",
          jars = None,
          args = None,
          driverCores = Some(1),
          driverMemory = Some("1g"),
          executorCores = Some(1),
          executorMemory = Some("1g"),
          numExecutors = Some(1),
          logLevel = Some("INFO"),
          None
        )
      )
    )
    assert(actual == expected)
  }

  test("map spark workload with custom config") {

    val dataProduct = DataProduct(
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
    )

    val workload = Workload[SparkCdpPrivate](
      id = "urn:dmb:cmp:domain:my-dp-id:1:wl-id",
      name = "my-dp-name",
      description = "my-dp-desc",
      version = "0.0.1",
      specific = SparkCdpPrivateJob(
        jobName = "my-job-name",
        jar = "hdfs://localhost:8020/jarfile.jar",
        className = "com.MyClass",
        jobConfig = Some(
          JobConfig(
            args = Some(Seq("my", "args")),
            dependencies = Some(
              Seq(
                "s3://bucket/dfile1.jar",
                "s3://bucket/dfile2.jar"
              )
            ),
            driverCores = Some(2),
            driverMemory = Some("2g"),
            executorCores = Some(2),
            executorMemory = Some("2g"),
            numExecutors = Some(2),
            schedule = Some(JobScheduler("5 * * * *", "x", "y")),
            logLevel = None,
            conf = None
          )
        ),
        queue = "default"
      )
    )

    val actual = sparkCdpPrivateJobMapper.map(dataProduct, workload)

    val expected = Right(
      SparkCdpPrivateWorkload(
        domain = "my-dp-domain",
        dataProduct = "my-dp-name",
        job = Job.spark(
          name = "my-job-name",
          resource = "mainres-7f59fed658cdded64d35590c3c86003d",
          filePath = "hdfs://localhost:8020/jarfile.jar",
          className = "com.MyClass",
          jars = Some(Seq("dfile1.jar", "dfile2.jar")),
          args = Some(Seq("my", "args")),
          driverCores = Some(2),
          driverMemory = Some("2g"),
          executorCores = Some(2),
          executorMemory = Some("2g"),
          numExecutors = Some(2),
          logLevel = Some("INFO"),
          schedule = Some(
            Schedule(enabled = true, Some(""), None, None, None, None, Some("x"), Some("y"), Some("5 * * * *"), None)
          )
        ),
        queue = "default"
      )
    )

    assert(actual == expected)
  }

}

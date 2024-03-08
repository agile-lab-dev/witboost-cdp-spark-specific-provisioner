package it.agilelab.provisioning.spark.workloads.provisioner.app.api.validate

import cats.data.Validated.{ invalidNel, valid }
import cats.data.{ NonEmptyList, Validated }
import it.agilelab.provisioning.commons.validator.ValidationFail
import it.agilelab.provisioning.mesh.self.service.api.model.Component._
import it.agilelab.provisioning.mesh.self.service.api.model.{ DataProduct, ProvisionRequest }
import it.agilelab.provisioning.spark.workloads.core.{ JobConfig, SparkCdpPrivate }
import it.agilelab.provisioning.spark.workloads.core.SparkCdpPrivate._
import it.agilelab.provisioning.spark.workloads.core.context.cdpPrivate.httpclient.KerberosHttpClient
import it.agilelab.provisioning.spark.workloads.core.models.DpCdp
import it.agilelab.provisioning.spark.workloads.provisioner.app.api.validate.SparkCdpPrivateValidator.validator
import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite

class SparkCdpPrivateValidatorTest extends AnyFunSuite with MockFactory {

  private val mockHttpClient: KerberosHttpClient = mock[KerberosHttpClient]

  private val specificSpark: SparkCdpPrivateJob = SparkCdpPrivateJob(
    jobName = "my-dm-my-dp-1-my-wl-my-dp-environment",
    jar = "hdfs://folder/my-jar",
    className = "my-class",
    jobConfig = None,
    queue = ""
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

    (mockHttpClient.executeGet _)
      .expects(where { (url: String) =>
        url.contains("webhdfs/v1")
      })
      .returning(
        """{"FileStatus":{"accessTime":1,"blockSize":1,"childrenNum":0,"fileId":1,"group":"supergroup","length":2017859,"modificationTime":1,"owner":"hdfs","pathSuffix":"","permission":"644","replication":3,"storagePolicy":0,"type":"FILE"}}"""
      )
      .once()

    (mockHttpClient.executeGet _)
      .expects(where { (url: String) =>
        url.contains("jmx?qry=Hadoop:service=NameNode,name=NameNodeStatus")
      })
      .returning(
        """{"beans":[{"name":"Hadoop:service=NameNode,name=NameNameStatus","modelerType":"org.apache.hadoop.hdfs.server.namenode.NameNode","State":"active","NNRole":"NameNode","HostAndPort":"x.x.cloudera.com:8020","SecurityEnabled":true,"LastHATransitionTime":5,"BytesWithFutureGenerationStamps":0,"SlowPeersReport":null,"SlowDisksReport":null}]}"""
      )
      .once()
    (mockHttpClient.executeGet _)
      .expects(where { (url: String) =>
        url.contains("jmx?qry=Hadoop:service=NameNode,name=NameNodeStatus")
      })
      .returning(
        """{"beans":[{"name":"Hadoop:service=NameNode,name=NameNameStatus","modelerType":"org.apache.hadoop.hdfs.server.namenode.NameNode","State":"active","NNRole":"NameNode","HostAndPort":"x.x.cloudera.com:8020","SecurityEnabled":true,"LastHATransitionTime":5,"BytesWithFutureGenerationStamps":0,"SlowPeersReport":null,"SlowDisksReport":null}]}"""
      )
      .once()

    val actual = validator(mockHttpClient).validate(ProvisionRequest(dataProduct, Some(workload)))
    assert(actual == Right(valid(ProvisionRequest(dataProduct, Some(workload)))))
  }

  test("validate return invalid with bad jobname") {

    (mockHttpClient.executeGet _)
      .expects(where { (url: String) =>
        url.contains("webhdfs/v1")
      })
      .returning(
        """{"FileStatus":{"accessTime":1,"blockSize":1,"childrenNum":0,"fileId":1,"group":"supergroup","length":2017859,"modificationTime":1,"owner":"hdfs","pathSuffix":"","permission":"644","replication":3,"storagePolicy":0,"type":"FILE"}}"""
      )
      .once()

    (mockHttpClient.executeGet _)
      .expects(where { (url: String) =>
        url.contains("jmx?qry=Hadoop:service=NameNode,name=NameNodeStatus")
      })
      .returning(
        """{"beans":[{"name":"Hadoop:service=NameNode,name=NameNameStatus","modelerType":"org.apache.hadoop.hdfs.server.namenode.NameNode","State":"active","NNRole":"NameNode","HostAndPort":"x.x.cloudera.com:8020","SecurityEnabled":true,"LastHATransitionTime":5,"BytesWithFutureGenerationStamps":0,"SlowPeersReport":null,"SlowDisksReport":null}]}"""
      )
      .once()
    (mockHttpClient.executeGet _)
      .expects(where { (url: String) =>
        url.contains("jmx?qry=Hadoop:service=NameNode,name=NameNodeStatus")
      })
      .returning(
        """{"beans":[{"name":"Hadoop:service=NameNode,name=NameNameStatus","modelerType":"org.apache.hadoop.hdfs.server.namenode.NameNode","State":"active","NNRole":"NameNode","HostAndPort":"x.x.cloudera.com:8020","SecurityEnabled":true,"LastHATransitionTime":5,"BytesWithFutureGenerationStamps":0,"SlowPeersReport":null,"SlowDisksReport":null}]}"""
      )
      .once()

    val s: SparkCdpPrivate = specificSpark.copy(jobName = "jn")
    val wl                 = workload.copy(specific = s)
    val actual             = validator(mockHttpClient).validate(ProvisionRequest(dataProduct, Some(wl)))
    assert(actual == Right(invalidNel(ValidationFail(ProvisionRequest(dataProduct, Some(wl)), "Job name not valid"))))
  }

  test("validate return invalid with wrong config") {

    (mockHttpClient.executeGet _)
      .expects(where { (url: String) =>
        url.contains("webhdfs/v1")
      })
      .returning(
        """{"FileStatus":{"accessTime":1,"blockSize":1,"childrenNum":0,"fileId":1,"group":"supergroup","length":2017859,"modificationTime":1,"owner":"hdfs","pathSuffix":"","permission":"644","replication":3,"storagePolicy":0,"type":"FILE"}}"""
      )
      .once()

    (mockHttpClient.executeGet _)
      .expects(where { (url: String) =>
        url.contains("jmx?qry=Hadoop:service=NameNode,name=NameNodeStatus")
      })
      .returning(
        """{"beans":[{"name":"Hadoop:service=NameNode,name=NameNameStatus","modelerType":"org.apache.hadoop.hdfs.server.namenode.NameNode","State":"active","NNRole":"NameNode","HostAndPort":"x.x.cloudera.com:8020","SecurityEnabled":true,"LastHATransitionTime":5,"BytesWithFutureGenerationStamps":0,"SlowPeersReport":null,"SlowDisksReport":null}]}"""
      )
      .once()
    (mockHttpClient.executeGet _)
      .expects(where { (url: String) =>
        url.contains("jmx?qry=Hadoop:service=NameNode,name=NameNodeStatus")
      })
      .returning(
        """{"beans":[{"name":"Hadoop:service=NameNode,name=NameNameStatus","modelerType":"org.apache.hadoop.hdfs.server.namenode.NameNode","State":"active","NNRole":"NameNode","HostAndPort":"x.x.cloudera.com:8020","SecurityEnabled":true,"LastHATransitionTime":5,"BytesWithFutureGenerationStamps":0,"SlowPeersReport":null,"SlowDisksReport":null}]}"""
      )
      .once()

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
    val actual             = validator(mockHttpClient).validate(ProvisionRequest(dataProduct, Some(wl)))

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

  test("validate return invalid with no active hdfs name node") {

    (mockHttpClient.executeGet _)
      .expects(where { (url: String) =>
        url.contains("jmx?qry=Hadoop:service=NameNode,name=NameNodeStatus")
      })
      .returning(
        """{"beans":[{"name":"Hadoop:service=NameNode,name=NameNameStatus","modelerType":"org.apache.hadoop.hdfs.server.namenode.NameNode","State":"standby","NNRole":"NameNode","HostAndPort":"x.x.cloudera.com:8020","SecurityEnabled":true,"LastHATransitionTime":5,"BytesWithFutureGenerationStamps":0,"SlowPeersReport":null,"SlowDisksReport":null}]}"""
      )

    (mockHttpClient.executeGet _)
      .expects(where { (url: String) =>
        url.contains("jmx?qry=Hadoop:service=NameNode,name=NameNodeStatus")
      })
      .returning(
        """{"beans":[{"name":"Hadoop:service=NameNode,name=NameNameStatus","modelerType":"org.apache.hadoop.hdfs.server.namenode.NameNode","State":"standby","NNRole":"NameNode","HostAndPort":"x.x.cloudera.com:8020","SecurityEnabled":true,"LastHATransitionTime":5,"BytesWithFutureGenerationStamps":0,"SlowPeersReport":null,"SlowDisksReport":null}]}"""
      )

    (mockHttpClient.executeGet _)
      .expects(where { (url: String) =>
        url.contains("jmx?qry=Hadoop:service=NameNode,name=NameNodeStatus")
      })
      .returning(
        """{"beans":[{"name":"Hadoop:service=NameNode,name=NameNameStatus","modelerType":"org.apache.hadoop.hdfs.server.namenode.NameNode","State":"standby","NNRole":"NameNode","HostAndPort":"x.x.cloudera.com:8020","SecurityEnabled":true,"LastHATransitionTime":5,"BytesWithFutureGenerationStamps":0,"SlowPeersReport":null,"SlowDisksReport":null}]}"""
      )

    (mockHttpClient.executeGet _)
      .expects(where { (url: String) =>
        url.contains("jmx?qry=Hadoop:service=NameNode,name=NameNodeStatus")
      })
      .returning(
        """{"beans":[{"name":"Hadoop:service=NameNode,name=NameNameStatus","modelerType":"org.apache.hadoop.hdfs.server.namenode.NameNode","State":"standby","NNRole":"NameNode","HostAndPort":"x.x.cloudera.com:8020","SecurityEnabled":true,"LastHATransitionTime":5,"BytesWithFutureGenerationStamps":0,"SlowPeersReport":null,"SlowDisksReport":null}]}"""
      )

    val actual = validator(mockHttpClient).validate(ProvisionRequest(dataProduct, Some(workload)))

    val expectedErrors = NonEmptyList
      .of(
        "Errors when connecting to hdfs NameNode",
        "Job Source application file not found"
      )
      .map(msg => ValidationFail(ProvisionRequest(dataProduct, Some(workload)), msg))

    val expected = Right(Validated.invalid(expectedErrors))

    assert(actual == expected)
  }

  test("validate return invalid with jar not found on hdfs") {

    (mockHttpClient.executeGet _)
      .expects(where { (url: String) =>
        url.contains("jmx?qry=Hadoop:service=NameNode,name=NameNodeStatus")
      })
      .returning(
        """{"beans":[{"name":"Hadoop:service=NameNode,name=NameNameStatus","modelerType":"org.apache.hadoop.hdfs.server.namenode.NameNode","State":"standby","NNRole":"NameNode","HostAndPort":"x.x.cloudera.com:8020","SecurityEnabled":true,"LastHATransitionTime":5,"BytesWithFutureGenerationStamps":0,"SlowPeersReport":null,"SlowDisksReport":null}]}"""
      )
    (mockHttpClient.executeGet _)
      .expects(where { (url: String) =>
        url.contains("jmx?qry=Hadoop:service=NameNode,name=NameNodeStatus")
      })
      .returning(
        """{"beans":[{"name":"Hadoop:service=NameNode,name=NameNameStatus","modelerType":"org.apache.hadoop.hdfs.server.namenode.NameNode","State":"active","NNRole":"NameNode","HostAndPort":"x.x.cloudera.com:8020","SecurityEnabled":true,"LastHATransitionTime":5,"BytesWithFutureGenerationStamps":0,"SlowPeersReport":null,"SlowDisksReport":null}]}"""
      )

    (mockHttpClient.executeGet _)
      .expects(where { (url: String) =>
        url.contains("jmx?qry=Hadoop:service=NameNode,name=NameNodeStatus")
      })
      .returning(
        """{"beans":[{"name":"Hadoop:service=NameNode,name=NameNameStatus","modelerType":"org.apache.hadoop.hdfs.server.namenode.NameNode","State":"standby","NNRole":"NameNode","HostAndPort":"x.x.cloudera.com:8020","SecurityEnabled":true,"LastHATransitionTime":5,"BytesWithFutureGenerationStamps":0,"SlowPeersReport":null,"SlowDisksReport":null}]}"""
      )
    (mockHttpClient.executeGet _)
      .expects(where { (url: String) =>
        url.contains("jmx?qry=Hadoop:service=NameNode,name=NameNodeStatus")
      })
      .returning(
        """{"beans":[{"name":"Hadoop:service=NameNode,name=NameNameStatus","modelerType":"org.apache.hadoop.hdfs.server.namenode.NameNode","State":"active","NNRole":"NameNode","HostAndPort":"x.x.cloudera.com:8020","SecurityEnabled":true,"LastHATransitionTime":5,"BytesWithFutureGenerationStamps":0,"SlowPeersReport":null,"SlowDisksReport":null}]}"""
      )

    (mockHttpClient.executeGet _)
      .expects(where { (url: String) =>
        url.contains("webhdfs/v1")
      })
      .returning(
        """{"FileStatus":{"accessTime":1,"blockSize":1,"childrenNum":0,"group":"supergroup","length":2017859,"modificationTime":1,"owner":"hdfs","pathSuffix":"","permission":"644","replication":3,"storagePolicy":0,"type":"FILE"}}"""
      )
      .once()

    val actual = validator(mockHttpClient).validate(ProvisionRequest(dataProduct, Some(workload)))

    val expectedErrors = NonEmptyList
      .of(
        "Job Source application file not found"
      )
      .map(msg => ValidationFail(ProvisionRequest(dataProduct, Some(workload)), msg))

    val expected = Right(Validated.invalid(expectedErrors))

    assert(actual == expected)
  }

  test("validate return valid with unknown jar scheme") {

    val specificSpark: SparkCdpPrivateJob = SparkCdpPrivateJob(
      jobName = "my-dm-my-dp-1-my-wl-my-dp-environment",
      jar = "unknown://folder/my-jar",
      className = "my-class",
      jobConfig = None,
      queue = ""
    )

    val workload: Workload[SparkCdpPrivate] = Workload[SparkCdpPrivate](
      id = "urn:dmb:cmp:my_dm.my_dp.1.my_wl",
      name = "my-dp-wl-name",
      description = "my-dp-desc",
      version = "my-dp-version",
      specific = specificSpark
    )

    val actual = validator(mockHttpClient).validate(ProvisionRequest(dataProduct, Some(workload)))
    assert(actual == Right(valid(ProvisionRequest(dataProduct, Some(workload)))))

  }
  test("validate return valid with local jar") {

    val specificSpark: SparkCdpPrivateJob = SparkCdpPrivateJob(
      jobName = "my-dm-my-dp-1-my-wl-my-dp-environment",
      jar = "api/src/test/resources/emptyFile.jar",
      className = "my-class",
      jobConfig = None,
      queue = ""
    )

    val workload: Workload[SparkCdpPrivate] = Workload[SparkCdpPrivate](
      id = "urn:dmb:cmp:my_dm.my_dp.1.my_wl",
      name = "my-dp-wl-name",
      description = "my-dp-desc",
      version = "my-dp-version",
      specific = specificSpark
    )

    val actual = validator(mockHttpClient).validate(ProvisionRequest(dataProduct, Some(workload)))
    assert(actual == Right(valid(ProvisionRequest(dataProduct, Some(workload)))))

  }

  test("validate return invalid with local jar") {

    val specificSpark: SparkCdpPrivateJob = SparkCdpPrivateJob(
      jobName = "my-dm-my-dp-1-my-wl-my-dp-environment",
      jar = "api/src/test/resources/invalid/emptyFile.jar",
      className = "my-class",
      jobConfig = None,
      queue = ""
    )

    val workload: Workload[SparkCdpPrivate] = Workload[SparkCdpPrivate](
      id = "urn:dmb:cmp:my_dm.my_dp.1.my_wl",
      name = "my-dp-wl-name",
      description = "my-dp-desc",
      version = "my-dp-version",
      specific = specificSpark
    )

    val actual = validator(mockHttpClient).validate(ProvisionRequest(dataProduct, Some(workload)))

    val expectedErrors = NonEmptyList
      .of(
        "Job Source application file not found"
      )
      .map(msg => ValidationFail(ProvisionRequest(dataProduct, Some(workload)), msg))

    val expected = Right(Validated.invalid(expectedErrors))

    assert(actual == expected)
  }

}

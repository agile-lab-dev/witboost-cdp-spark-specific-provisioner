package it.agilelab.provisioning.spark.workloads.provisioner.app.service.gateway.mapper

import com.cloudera.cdp.de.model.{ ServiceDescription, VcDescription }
import it.agilelab.provisioning.commons.client.cdp.de.CdpDeClient
import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.base.{ Job, PythonEnvironment, Schedule }
import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.request.{ CreateResourceReq, UploadFileReq }
import it.agilelab.provisioning.mesh.self.service.api.model.Component.Workload
import it.agilelab.provisioning.mesh.self.service.api.model.DataProduct
import it.agilelab.provisioning.spark.workload.core.SparkCde._
import it.agilelab.provisioning.spark.workload.core.models.DpCdp
import it.agilelab.provisioning.spark.workload.core.{ JobConfig, JobScheduler }
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.artifactory.ArtifactoryGateway
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.idbroker.IdBrokerHostsProvider
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.mapper.SparkCdeWorkloadMapper
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.workload.SparkCdeWorkload
import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite

class SparkCdeWorkloadMapperTest extends AnyFunSuite with MockFactory {

  val cdpDeClient: CdpDeClient                     = mock[CdpDeClient]
  val idBrokerHostsProvider: IdBrokerHostsProvider = mock[IdBrokerHostsProvider]
  val artifactoryGateway: ArtifactoryGateway       = mock[ArtifactoryGateway]

  val sparkCdeJobMapper = new SparkCdeWorkloadMapper(cdpDeClient, idBrokerHostsProvider, artifactoryGateway)

  val srvDesc = new ServiceDescription()
  srvDesc.setName("serviceXX")
  srvDesc.setClusterId("serviceXX-id")
  srvDesc.setClusterFqdn("my-cluster-fqdn")

  val vcDesc = new VcDescription()
  vcDesc.setClusterId("serviceXX-id")
  vcDesc.setVcName("clusterXX")
  vcDesc.setVcId("clusterXX-id")
  vcDesc.setVcApiUrl("https://my-api")

  test("map spark workload") {
    inSequence(
      (cdpDeClient.describeServiceByName _)
        .expects("my-cde-service")
        .once()
        .returns(Right(srvDesc)),
      (cdpDeClient.describeVcByName _)
        .expects("serviceXX-id", "my-cde-cluster")
        .once()
        .returns(Right(vcDesc)),
      (idBrokerHostsProvider.get _)
        .expects(srvDesc)
        .once()
        .returns(Right(Seq("host1", "host2"))),
      (artifactoryGateway.get _)
        .expects("s3://bucket/jarfile.jar")
        .once()
        .returns(Right(Array.emptyByteArray))
    )

    val actual   = sparkCdeJobMapper.map(
      DataProduct(
        id = "urn:dmb:cmp:my-dp-id",
        name = "my-dp-name",
        domain = "my-dp-domain",
        environment = "dev",
        version = "1",
        dataProductOwner = "dpOwner",
        specific = new DpCdp,
        components = Seq()
      ),
      Workload(
        id = "urn:dmb:cmp:domain:my-dp-id:1:wl-id",
        name = "my-dp-name",
        description = "my-dp-desc",
        version = "0.0.1",
        specific = SparkCdeJob(
          cdeService = "my-cde-service",
          cdeCluster = "my-cde-cluster",
          jobName = "my-job-name",
          jar = "s3://bucket/jarfile.jar",
          className = "com.MyClass",
          jobConfig = None
        )
      )
    )
    val expected = Right(
      SparkCdeWorkload(
        domain = "my-dp-domain",
        dataProduct = "my-dp-name",
        service = srvDesc,
        vc = vcDesc,
        resources = Seq(
          CreateResourceReq("mainres-7f59fed658cdded64d35590c3c86003d", "files", "keep_indefinitely", None)
        ),
        artifacts = Seq(
          UploadFileReq(
            "mainres-7f59fed658cdded64d35590c3c86003d",
            "jarfile.jar",
            "application/java-archive",
            Array.emptyByteArray
          )
        ),
        job = Job.spark(
          name = "my-job-name",
          resource = "mainres-7f59fed658cdded64d35590c3c86003d",
          filePath = "jarfile.jar",
          className = "com.MyClass",
          jars = None,
          args = None,
          driverCores = Some(1),
          driverMemory = Some("1g"),
          executorCores = Some(1),
          executorMemory = Some("1g"),
          numExecutors = Some(1),
          logLevel = Some("INFO"),
          conf = Some(Map("dex.safariEnabled" -> "true", "spark.cde.idBrokerHosts" -> "host1,host2")),
          None
        )
      )
    )
    assert(actual == expected)
  }

  test("map spark workload with longest name") {
    inSequence(
      (cdpDeClient.describeServiceByName _)
        .expects("my-cde-service")
        .once()
        .returns(Right(srvDesc)),
      (cdpDeClient.describeVcByName _)
        .expects("serviceXX-id", "my-cde-cluster")
        .once()
        .returns(Right(vcDesc)),
      (idBrokerHostsProvider.get _)
        .expects(srvDesc)
        .once()
        .returns(Right(Seq("host1", "host2"))),
      (artifactoryGateway.get _)
        .expects("s3://bucket/jarfile.jar")
        .once()
        .returns(Right(Array.emptyByteArray))
    )

    val actual   = sparkCdeJobMapper.map(
      DataProduct(
        id = "urn:dmb:cmp:my-dp-id",
        name = "my-dp-name",
        domain = "my-dp-domain",
        environment = "dev",
        version = "1",
        dataProductOwner = "dpOwner",
        specific = new DpCdp,
        components = Seq()
      ),
      Workload(
        id = "urn:dmb:cmp:domain:my-dp-id-super-mega-longest-name-bla-bla-bla-bla:1:wl-id",
        name = "my-dp-name",
        description = "my-dp-desc",
        version = "0.0.1",
        specific = SparkCdeJob(
          cdeService = "my-cde-service",
          cdeCluster = "my-cde-cluster",
          jobName = "super-mega-longest-name-bla-bla-bla-bla-bla-bla-bla-bla-bla-bla-bla-bla",
          jar = "s3://bucket/jarfile.jar",
          className = "com.MyClass",
          jobConfig = None
        )
      )
    )
    val expected = Right(
      gateway.workload.SparkCdeWorkload(
        domain = "my-dp-domain",
        dataProduct = "my-dp-name",
        service = srvDesc,
        vc = vcDesc,
        resources = Seq(
          CreateResourceReq("mainres-80d4444a3ec0de0e6b5c677fdfcbef7f", "files", "keep_indefinitely", None)
        ),
        artifacts = Seq(
          UploadFileReq(
            "mainres-80d4444a3ec0de0e6b5c677fdfcbef7f",
            "jarfile.jar",
            "application/java-archive",
            Array.emptyByteArray
          )
        ),
        job = Job.spark(
          name = "super-mega-longest-name-bla-bla-bla-bla-bla-bla-bla-bla-bla-bla-bla-bla",
          resource = "mainres-80d4444a3ec0de0e6b5c677fdfcbef7f",
          filePath = "jarfile.jar",
          className = "com.MyClass",
          jars = None,
          args = None,
          driverCores = Some(1),
          driverMemory = Some("1g"),
          executorCores = Some(1),
          executorMemory = Some("1g"),
          numExecutors = Some(1),
          logLevel = Some("INFO"),
          conf = Some(Map("dex.safariEnabled" -> "true", "spark.cde.idBrokerHosts" -> "host1,host2")),
          None
        )
      )
    )
    assert(actual == expected)
  }

  test("map spark workload with custom config") {
    inSequence(
      (cdpDeClient.describeServiceByName _)
        .expects("my-cde-service")
        .once()
        .returns(Right(srvDesc)),
      (cdpDeClient.describeVcByName _)
        .expects("serviceXX-id", "my-cde-cluster")
        .once()
        .returns(Right(vcDesc)),
      (idBrokerHostsProvider.get _)
        .expects(srvDesc)
        .once()
        .returns(Right(Seq("host1", "host2"))),
      (artifactoryGateway.get _)
        .expects("s3://bucket/jarfile.jar")
        .once()
        .returns(Right(Array.emptyByteArray)),
      (artifactoryGateway.get _)
        .expects("s3://bucket/dfile1.jar")
        .once()
        .returns(Right(Array.emptyByteArray)),
      (artifactoryGateway.get _)
        .expects("s3://bucket/dfile2.jar")
        .once()
        .returns(Right(Array.emptyByteArray))
    )

    val actual   = sparkCdeJobMapper.map(
      DataProduct(
        id = "urn:dmb:cmp:my-dp-id",
        name = "my-dp-name",
        domain = "my-dp-domain",
        environment = "dev",
        version = "1",
        dataProductOwner = "dpOwner",
        specific = new DpCdp,
        components = Seq()
      ),
      Workload(
        id = "urn:dmb:cmp:domain:my-dp-id:1:wl-id",
        name = "my-dp-name",
        description = "my-dp-desc",
        version = "0.0.1",
        specific = SparkCdeJob(
          cdeService = "my-cde-service",
          cdeCluster = "my-cde-cluster",
          jobName = "my-job-name",
          jar = "s3://bucket/jarfile.jar",
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
          )
        )
      )
    )
    val expected = Right(
      gateway.workload.SparkCdeWorkload(
        domain = "my-dp-domain",
        dataProduct = "my-dp-name",
        service = srvDesc,
        vc = vcDesc,
        resources = Seq(
          CreateResourceReq("mainres-7f59fed658cdded64d35590c3c86003d", "files", "keep_indefinitely", None)
        ),
        artifacts = Seq(
          UploadFileReq(
            "mainres-7f59fed658cdded64d35590c3c86003d",
            "jarfile.jar",
            "application/java-archive",
            Array.emptyByteArray
          ),
          UploadFileReq(
            "mainres-7f59fed658cdded64d35590c3c86003d",
            "dfile1.jar",
            "application/java-archive",
            Array.emptyByteArray
          ),
          UploadFileReq(
            "mainres-7f59fed658cdded64d35590c3c86003d",
            "dfile2.jar",
            "application/java-archive",
            Array.emptyByteArray
          )
        ),
        job = Job.spark(
          name = "my-job-name",
          resource = "mainres-7f59fed658cdded64d35590c3c86003d",
          filePath = "jarfile.jar",
          className = "com.MyClass",
          jars = Some(Seq("dfile1.jar", "dfile2.jar")),
          args = Some(Seq("my", "args")),
          driverCores = Some(2),
          driverMemory = Some("2g"),
          executorCores = Some(2),
          executorMemory = Some("2g"),
          numExecutors = Some(2),
          logLevel = Some("INFO"),
          conf = Some(Map("dex.safariEnabled" -> "true", "spark.cde.idBrokerHosts" -> "host1,host2")),
          schedule = Some(
            Schedule(enabled = true, Some(""), None, None, None, None, Some("x"), Some("y"), Some("5 * * * *"), None)
          )
        )
      )
    )
    assert(actual == expected)
  }

  test("map pyspark workload") {
    inSequence(
      (cdpDeClient.describeServiceByName _)
        .expects("my-cde-service")
        .once()
        .returns(Right(srvDesc)),
      (cdpDeClient.describeVcByName _)
        .expects("serviceXX-id", "my-cde-cluster")
        .once()
        .returns(Right(vcDesc)),
      (idBrokerHostsProvider.get _)
        .expects(srvDesc)
        .once()
        .returns(Right(Seq("host1", "host2"))),
      (artifactoryGateway.get _)
        .expects("s3://bucket/app.py")
        .once()
        .returns(Right(Array.emptyByteArray))
    )

    val actual   = sparkCdeJobMapper.map(
      DataProduct(
        id = "urn:dmb:cmp:my-dp-id",
        name = "my-dp-name",
        domain = "my-dp-domain",
        environment = "dev",
        version = "1",
        dataProductOwner = "dpOwner",
        specific = new DpCdp,
        components = Seq()
      ),
      Workload(
        id = "urn:dmb:cmp:domain:my-dp-id:1:wl-id",
        name = "my-dp-name",
        description = "my-dp-desc",
        version = "0.0.1",
        specific = PySparkCdeJob(
          cdeService = "my-cde-service",
          cdeCluster = "my-cde-cluster",
          jobName = "my-job-name",
          pythonFile = "s3://bucket/app.py",
          pythonVersion = "3.7",
          jobConfig = None
        )
      )
    )
    val expected = Right(
      gateway.workload.SparkCdeWorkload(
        domain = "my-dp-domain",
        dataProduct = "my-dp-name",
        service = srvDesc,
        vc = vcDesc,
        resources = Seq(
          CreateResourceReq("mainres-7f59fed658cdded64d35590c3c86003d", "files", "keep_indefinitely", None)
        ),
        artifacts = Seq(
          UploadFileReq("mainres-7f59fed658cdded64d35590c3c86003d", "app.py", "text/plain", Array.emptyByteArray)
        ),
        job = Job.pyspark(
          name = "my-job-name",
          resource = "mainres-7f59fed658cdded64d35590c3c86003d",
          filePath = "app.py",
          pyFiles = None,
          args = None,
          driverCores = Some(1),
          driverMemory = Some("1g"),
          executorCores = Some(1),
          executorMemory = Some("1g"),
          numExecutors = Some(1),
          logLevel = Some("INFO"),
          conf = Some(
            Map(
              "dex.safariEnabled"       -> "true",
              "spark.cde.idBrokerHosts" -> "host1,host2",
              "spark.pyspark.python"    -> "3.7"
            )
          ),
          schedule = None
        )
      )
    )
    assert(actual == expected)
  }

  test("map pyspark workload with pyenv and dependencies") {
    inSequence(
      (cdpDeClient.describeServiceByName _)
        .expects("my-cde-service")
        .once()
        .returns(Right(srvDesc)),
      (cdpDeClient.describeVcByName _)
        .expects("serviceXX-id", "my-cde-cluster")
        .once()
        .returns(Right(vcDesc)),
      (idBrokerHostsProvider.get _)
        .expects(srvDesc)
        .once()
        .returns(Right(Seq("host1", "host2"))),
      (artifactoryGateway.get _)
        .expects("s3://bucket/app.py")
        .once()
        .returns(Right(Array.emptyByteArray)),
      (artifactoryGateway.get _)
        .expects("s3://bucket/dep1.py")
        .once()
        .returns(Right(Array.emptyByteArray)),
      (artifactoryGateway.get _)
        .expects("s3://bucket/dep2.py")
        .once()
        .returns(Right(Array.emptyByteArray)),
      (artifactoryGateway.get _)
        .expects("s3://bucket/requirements.txt")
        .once()
        .returns(Right(Array.emptyByteArray))
    )

    val actual   = sparkCdeJobMapper.map(
      DataProduct(
        id = "urn:dmb:cmp:my-dp-id",
        name = "my-dp-name",
        domain = "my-dp-domain",
        environment = "dev",
        version = "1",
        dataProductOwner = "dpOwner",
        specific = new DpCdp,
        components = Seq()
      ),
      Workload(
        id = "urn:dmb:cmp:domain:my-dp-id:1:wl-id",
        name = "my-dp-name",
        description = "my-dp-desc",
        version = "0.0.1",
        specific = PySparkCdeJobWithPyEnv(
          cdeService = "my-cde-service",
          cdeCluster = "my-cde-cluster",
          jobName = "my-job-name",
          pythonFile = "s3://bucket/app.py",
          pythonVersion = "3.7",
          requirementsFile = "s3://bucket/requirements.txt",
          pyMirror = Some("pyMirror"),
          jobConfig = Some(
            JobConfig(
              args = None,
              dependencies = Some(
                Seq(
                  "s3://bucket/dep1.py",
                  "s3://bucket/dep2.py"
                )
              ),
              driverCores = None,
              driverMemory = None,
              executorCores = None,
              executorMemory = None,
              numExecutors = None,
              logLevel = None,
              conf = None,
              schedule = None
            )
          )
        )
      )
    )
    val expected = Right(
      gateway.workload.SparkCdeWorkload(
        domain = "my-dp-domain",
        dataProduct = "my-dp-name",
        service = srvDesc,
        vc = vcDesc,
        resources = Seq(
          CreateResourceReq("mainres-7f59fed658cdded64d35590c3c86003d", "files", "keep_indefinitely", None),
          CreateResourceReq(
            "mainres-7f59fed658cdded64d35590c3c86003d",
            "python-env",
            "keep_indefinitely",
            Some(PythonEnvironment("3.7", Some("pyMirror")))
          )
        ),
        artifacts = Seq(
          UploadFileReq("mainres-7f59fed658cdded64d35590c3c86003d", "app.py", "text/plain", Array.emptyByteArray),
          UploadFileReq("mainres-7f59fed658cdded64d35590c3c86003d", "dep1.py", "text/plain", Array.emptyByteArray),
          UploadFileReq("mainres-7f59fed658cdded64d35590c3c86003d", "dep2.py", "text/plain", Array.emptyByteArray),
          UploadFileReq(
            "pyenvres-7f59fed658cdded64d35590c3c86003d",
            "requirements.txt",
            "text/plain",
            Array.emptyByteArray
          )
        ),
        job = Job.pyspark(
          name = "my-job-name",
          resource = "mainres-7f59fed658cdded64d35590c3c86003d",
          filePath = "app.py",
          pyFiles = Some(Seq("dep1.py", "dep2.py")),
          args = None,
          driverCores = Some(1),
          driverMemory = Some("1g"),
          executorCores = Some(1),
          executorMemory = Some("1g"),
          numExecutors = Some(1),
          logLevel = Some("INFO"),
          conf = Some(
            Map(
              "dex.safariEnabled"       -> "true",
              "spark.cde.idBrokerHosts" -> "host1,host2",
              "spark.pyspark.python"    -> "3.7"
            )
          ),
          schedule = None,
          pythonEnvResourceName = Some("pyenvres-7f59fed658cdded64d35590c3c86003d")
        )
      )
    )
    assert(actual.getOrElse(fail()).job == expected.getOrElse(fail()).job)
  }

  test("map pyspark workload with longest name") {
    inSequence(
      (cdpDeClient.describeServiceByName _)
        .expects("my-cde-service")
        .once()
        .returns(Right(srvDesc)),
      (cdpDeClient.describeVcByName _)
        .expects("serviceXX-id", "my-cde-cluster")
        .once()
        .returns(Right(vcDesc)),
      (idBrokerHostsProvider.get _)
        .expects(srvDesc)
        .once()
        .returns(Right(Seq("host1", "host2"))),
      (artifactoryGateway.get _)
        .expects("s3://bucket/app.py")
        .once()
        .returns(Right(Array.emptyByteArray))
    )

    val actual   = sparkCdeJobMapper.map(
      DataProduct(
        id = "urn:dmb:cmp:my-dp-id",
        name = "my-dp-name",
        domain = "my-dp-domain",
        environment = "dev",
        version = "1",
        dataProductOwner = "dpOwner",
        specific = new DpCdp,
        components = Seq()
      ),
      Workload(
        id = "urn:dmb:cmp:domain:my-dp-id-super-mega-longest-name-bla-bla-bla-bla:1:wl-id",
        name = "my-dp-name",
        description = "my-dp-desc",
        version = "0.0.1",
        specific = PySparkCdeJob(
          cdeService = "my-cde-service",
          cdeCluster = "my-cde-cluster",
          jobName = "super-mega-longest-name-bla-bla-bla-bla-bla-bla-bla-bla-bla-bla-bla-bla",
          pythonFile = "s3://bucket/app.py",
          pythonVersion = "3.7",
          jobConfig = None
        )
      )
    )
    val expected = Right(
      gateway.workload.SparkCdeWorkload(
        domain = "my-dp-domain",
        dataProduct = "my-dp-name",
        service = srvDesc,
        vc = vcDesc,
        resources = Seq(
          CreateResourceReq("mainres-80d4444a3ec0de0e6b5c677fdfcbef7f", "files", "keep_indefinitely", None)
        ),
        artifacts = Seq(
          UploadFileReq("mainres-80d4444a3ec0de0e6b5c677fdfcbef7f", "app.py", "text/plain", Array.emptyByteArray)
        ),
        job = Job.pyspark(
          name = "super-mega-longest-name-bla-bla-bla-bla-bla-bla-bla-bla-bla-bla-bla-bla",
          resource = "mainres-80d4444a3ec0de0e6b5c677fdfcbef7f",
          filePath = "app.py",
          pyFiles = None,
          args = None,
          driverCores = Some(1),
          driverMemory = Some("1g"),
          executorCores = Some(1),
          executorMemory = Some("1g"),
          numExecutors = Some(1),
          logLevel = Some("INFO"),
          conf = Some(
            Map(
              "dex.safariEnabled"       -> "true",
              "spark.cde.idBrokerHosts" -> "host1,host2",
              "spark.pyspark.python"    -> "3.7"
            )
          ),
          schedule = None
        )
      )
    )
    assert(actual == expected)
  }

}

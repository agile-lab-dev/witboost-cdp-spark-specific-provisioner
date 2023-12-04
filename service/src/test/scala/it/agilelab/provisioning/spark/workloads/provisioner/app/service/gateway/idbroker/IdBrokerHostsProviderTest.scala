package it.agilelab.provisioning.spark.workloads.provisioner.app.service.gateway.idbroker

import com.cloudera.cdp.datalake.model.{ Datalake, DatalakeDetails, Instance, InstanceGroup }
import com.cloudera.cdp.de.model.ServiceDescription
import it.agilelab.provisioning.commons.client.cdp.dl.CdpDlClientError.DescribeDlErr
import it.agilelab.provisioning.commons.client.cdp.dl.{ CdpDlClient, CdpDlClientError }
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.idbroker.IdBrokerHostsProvider
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.idbroker.IdBrokerHostsProviderError.{
  FindDlErr,
  NoDatalakeFound,
  NoIdBrokerInstanceGroupsFound
}
import org.scalamock.scalatest.MockFactory
import org.scalatest.EitherValues
import org.scalatest.funsuite.AnyFunSuite

import scala.jdk.CollectionConverters.SeqHasAsJava

class IdBrokerHostsProviderTest extends AnyFunSuite with MockFactory with EitherValues {

  val cdpDlClient: CdpDlClient = stub[CdpDlClient]
  val idBrokerHostProvider     = new IdBrokerHostsProvider(cdpDlClient)

  val idBrokerInstance      = new Instance
  idBrokerInstance.setId("foo")
  idBrokerInstance.setPrivateIp("my idBroker host")
  val idBrokerInstanceGroup = new InstanceGroup
  idBrokerInstanceGroup.setName("idbroker")
  idBrokerInstanceGroup.setInstances(Seq(idBrokerInstance).asJava)

  val dlDetails = new DatalakeDetails
  dlDetails.setDatalakeName("dlName")
  dlDetails.setEnvironmentCrn("crn")
  dlDetails.setInstanceGroups(Seq(idBrokerInstanceGroup).asJava)

  val srv = new ServiceDescription
  srv.setEnvironmentCrn("crn")

  test("retrieve private ip from a single idbroker host") {
    val dl1 = new Datalake
    dl1.setEnvironmentCrn("crn")
    dl1.setDatalakeName("dlName")

    (cdpDlClient.findAllDl _)
      .when()
      .returns(Right(Seq(dl1)))

    (cdpDlClient.describeDl _)
      .when("dlName")
      .returns(Right(dlDetails))

    assert(idBrokerHostProvider.get(srv) == Right(Seq("my idBroker host")))

  }

  test("return error when no DataLakeDetails is found in CDP") {
    (cdpDlClient.findAllDl _)
      .when()
      .returns(Left(CdpDlClientError.FindAllDlErr(new IllegalArgumentException("x"))))

    val actual = idBrokerHostProvider.get(srv)
    assert(actual.isLeft)
    assert(actual.left.value.isInstanceOf[FindDlErr])
  }

  test("return error when no DataLake matches the ServiceDescription") {
    (cdpDlClient.findAllDl _)
      .when()
      .returns(Right(Seq()))

    val actual = idBrokerHostProvider.get(srv)
    assert(actual.isLeft)
    assert(actual.left.value.isInstanceOf[NoDatalakeFound])
  }

  test("return error when no DataLakeDetails are available") {
    val dl = new Datalake
    dl.setDatalakeName("dlName")
    dl.setEnvironmentCrn("crn")

    (cdpDlClient.findAllDl _)
      .when()
      .returns(Right(Seq(dl)))

    (cdpDlClient.describeDl _)
      .when("dlName")
      .returns(Left(DescribeDlErr("x", new IllegalArgumentException("e"))))

    val actual = idBrokerHostProvider.get(srv)
    assert(actual.isLeft)
    assert(actual.left.value.isInstanceOf[FindDlErr])
  }

  test("return error when no idbroker InstanceGroup is found") {
    val dl = new Datalake
    dl.setDatalakeName("dlName")
    dl.setEnvironmentCrn("crn")

    val idBrokerInstance      = new Instance
    idBrokerInstance.setId("foo")
    idBrokerInstance.setPrivateIp("my idBroker host")
    val idBrokerInstanceGroup = new InstanceGroup
    idBrokerInstanceGroup.setName("other")
    idBrokerInstanceGroup.setInstances(Seq(idBrokerInstance).asJava)

    val dlDetailsWithoutIdBroker = new DatalakeDetails
    dlDetails.setDatalakeName("dlName")
    dlDetails.setEnvironmentCrn("crn")
    dlDetails.setInstanceGroups(Seq(idBrokerInstanceGroup).asJava)

    (cdpDlClient.findAllDl _)
      .when()
      .returns(Right(Seq(dl)))

    (cdpDlClient.describeDl _)
      .when("dlName")
      .returns(Right(dlDetailsWithoutIdBroker))

    val actual = idBrokerHostProvider.get(srv)
    assert(actual.isLeft)
    assert(actual.left.value == NoIdBrokerInstanceGroupsFound)
  }
}

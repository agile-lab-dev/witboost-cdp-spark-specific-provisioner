package it.agilelab.provisioning.spark.workloads.provisioner.app.service.gateway.clients

import it.agilelab.provisioning.commons.client.cdp.de.cluster.CdeClusterClient
import it.agilelab.provisioning.commons.http.Auth.BasicCredential
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.clients.CdeClientFactory
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.credential.CredentialProvider
import org.scalamock.scalatest.MockFactory
import org.scalatest.EitherValues
import org.scalatest.funsuite.AnyFunSuite

class CdeClientFactoryTest extends AnyFunSuite with MockFactory with EitherValues {

  test("create") {
    val credentialProvider = mock[CredentialProvider]
    (credentialProvider.get _)
      .expects("dm", "dp")
      .once()
      .returns(Right(BasicCredential("x", "y")))
    val cdeClient          = new CdeClientFactory(credentialProvider).create("dm", "dp")

    assert(cdeClient.exists(_.isInstanceOf[CdeClusterClient]))
  }

}

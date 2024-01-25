package it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.idbroker

import cats.implicits.{ catsSyntaxEq, _ }
import com.cloudera.cdp.de.model.ServiceDescription
import it.agilelab.provisioning.commons.client.cdp.dl.CdpDlClient
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.idbroker.IdBrokerHostsProviderError.{
  FindDlErr,
  NoDatalakeFound,
  NoIdBrokerInstanceGroupsFound
}

import scala.jdk.CollectionConverters.CollectionHasAsScala

class IdBrokerHostsProvider(cdpDlClient: CdpDlClient) {

  def get(srv: ServiceDescription): Either[IdBrokerHostsProviderError, Seq[String]] =
    for {
      dls                    <- cdpDlClient.findAllDl().leftMap(e => FindDlErr(e))
      dl                     <- dls
                                  .find(_.getEnvironmentCrn === srv.getEnvironmentCrn)
                                  .toRight(NoDatalakeFound(s"No Datalake matches the environment crn ${srv.getEnvironmentCrn}"))
      dlDesc                 <- cdpDlClient.describeDl(dl.getDatalakeName).leftMap(e => FindDlErr(e))
      idBrokerInstanceGroups <- dlDesc.getInstanceGroups.asScala.toSeq
                                  .find(_.getName === "idbroker")
                                  .toRight(NoIdBrokerInstanceGroupsFound)
      ips                     = idBrokerInstanceGroups.getInstances.asScala.flatMap(i => Option(i.getPrivateIp))
    } yield ips.toSeq
}

object IdBrokerHostsProvider {
  def create(client: CdpDlClient): IdBrokerHostsProvider =
    new IdBrokerHostsProvider(client)

}

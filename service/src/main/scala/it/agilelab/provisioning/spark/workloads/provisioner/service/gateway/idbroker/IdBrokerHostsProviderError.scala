package it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.idbroker

import cats.Show
import cats.implicits._
import it.agilelab.provisioning.commons.client.cdp.dl.CdpDlClientError

sealed trait IdBrokerHostsProviderError extends Exception with Product with Serializable

object IdBrokerHostsProviderError {
  final case class FindDlErr(error: CdpDlClientError) extends IdBrokerHostsProviderError
  final case class NoDatalakeFound(message: String)   extends IdBrokerHostsProviderError
  final case object NoIdBrokerInstanceGroupsFound     extends IdBrokerHostsProviderError

  implicit def showIdBrokerHostsProviderError: Show[IdBrokerHostsProviderError] = Show.show {
    case FindDlErr(error)              => show"FindDlErr($error)"
    case NoDatalakeFound(message)      => show"NoDatalakeFound($message)"
    case NoIdBrokerInstanceGroupsFound => show"NoIdBrokerInstanceGroupsFound"
  }
}

package it.agilelab.provisioning.spark.workloads.core.gateway.cdp

import it.agilelab.provisioning.commons.client.cdp.dl.CdpDlClientError
import it.agilelab.provisioning.commons.client.cdp.env.CdpEnvClientError

trait CdpGatewayError extends Exception with Product with Serializable

object CdpGatewayError {
  final case class CdpGatewayInitErr(error: Throwable)          extends CdpGatewayError
  final case class DescribeCdpEnvErr(error: CdpEnvClientError)  extends CdpGatewayError
  final case class DescribeCdpDlErr(error: CdpDlClientError)    extends CdpGatewayError
  final case class DatalakeNotFound(environment: String)        extends CdpGatewayError
  final case class StorageLocationNotFound(environment: String) extends CdpGatewayError
}

package it.agilelab.provisioning.spark.workloads.core.gateway.cdp

import cats.implicits.{ toBifunctorOps, toShow }
import com.typesafe.scalalogging.Logger
import it.agilelab.provisioning.commons.client.cdp.dl.CdpDlClient
import it.agilelab.provisioning.commons.client.cdp.env.CdpEnvClient
import CdpGatewayError.CdpGatewayInitErr

/** CdpGateway
  *
  * Provide a method to retrieve the storage location base from environment name
  */
trait CdpGateway {

  /** Retrieve the Storage base location configure for a specific environment datalake
    * @param environmentName: EnvironmentName
    * @return Right(StorageSpace) or Left(CdpGatewayError)
    */
  def getStorageLocationBase(environmentName: String): Either[CdpGatewayError, String]
}

object CdpGateway {
  private val logger = Logger(getClass.getName)

  /** DefaultCdpGateway
    * @return
    */
  def default(): Either[CdpGatewayError, CdpGateway] =
    for {
      cdpEnvClient <- CdpEnvClient.defaultWithAudit().leftMap { e =>
                        logger.error(e.show)
                        CdpGatewayInitErr(e)
                      }
      cdpDlClient  <- CdpDlClient.defaultWithAudit().leftMap { e =>
                        logger.error(e.show)
                        CdpGatewayInitErr(e)
                      }
    } yield new DefaultCdpGateway(cdpEnvClient, cdpDlClient)
}

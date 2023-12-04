package it.agilelab.provisioning.spark.workloads.provisioner.app.api.context

import cats.implicits.toBifunctorOps
import it.agilelab.provisioning.aws.s3.gateway.S3Gateway
import it.agilelab.provisioning.commons.client.cdp.de.CdpDeClient
import it.agilelab.provisioning.commons.config.Conf
import it.agilelab.provisioning.spark.workload.core.context.ContextError
import it.agilelab.provisioning.spark.workload.core.context.ContextError._

final case class ValidatorContext(
  cdpDeClient: CdpDeClient,
  s3Gateway: S3Gateway
)

object ValidatorContext {
  def init(conf: Conf): Either[ContextError, ValidatorContext] =
    for {
      cdpDeClient <- CdpDeClient.defaultWithAudit().leftMap(e => ClientError("CdpDeClient", e))
      s3Gateway   <- S3Gateway.defaultWithAudit().leftMap(e => ClientError("S3Gateway", e))
    } yield ValidatorContext(
      cdpDeClient,
      s3Gateway
    )
}

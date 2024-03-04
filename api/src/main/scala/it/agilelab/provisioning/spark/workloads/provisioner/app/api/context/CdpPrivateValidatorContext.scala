package it.agilelab.provisioning.spark.workloads.provisioner.app.api.context

import it.agilelab.provisioning.commons.config.Conf
import it.agilelab.provisioning.spark.workloads.core.context.ContextError

//Left intentionally blank, we will use it when implementing the connection with HDFS
final case class CdpPrivateValidatorContext()

object CdpPrivateValidatorContext {
  def init(conf: Conf): Either[ContextError, CdpPrivateValidatorContext] =
    Right(CdpPrivateValidatorContext())
}

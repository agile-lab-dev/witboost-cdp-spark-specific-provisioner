package it.agilelab.provisioning.spark.workload.core.context

import it.agilelab.provisioning.commons.config.ConfError

sealed trait ContextError extends Product with Serializable

object ContextError {
  final case class ConfigurationError(error: ConfError)              extends ContextError
  final case class ClientError(client: String, throwable: Throwable) extends ContextError
}

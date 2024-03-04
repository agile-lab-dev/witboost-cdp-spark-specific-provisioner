package it.agilelab.provisioning.spark.workloads.provisioner.service.config

import it.agilelab.provisioning.spark.workloads.core.context.cdpPrivate.{
  ApplicationConfigurationCore,
  ConfigurationModelCore
}

/** A private implementation of [[ConfigurationModelCore]] to be used only inside the framework.
  */
private[service] object ApplicationConfigurationService extends ApplicationConfigurationCore

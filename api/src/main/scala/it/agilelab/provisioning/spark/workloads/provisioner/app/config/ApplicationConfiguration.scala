package it.agilelab.provisioning.spark.workloads.provisioner.app.config

import com.typesafe.config.{ Config, ConfigFactory, ConfigRenderOptions }

/** This is a wrapper for the Typesafe [[Config]] class. A Specific Provisioner app
  * can extend this trait to obtain the main configuration values.
  */
trait ApplicationConfiguration extends ConfigurationModel {

  /** The whole configuration starting from the "datameshProvisioner" key in the configuration files.
    */
  lazy val provisionerConfig: Config = ConfigFactory.load().getConfig(PROVISIONER)

  private val formatter = ConfigRenderOptions.concise().setFormatted(true)

  /** Create a human readable version of the given [[Config]] object formatted as JSON
    *
    * @return a [[String]] representing the given [[Config]] object formatted as JSON
    */
  def printBeautifiedConfigJSON(): String = provisionerConfig.root().render(formatter)

}

/** A private implementation of [[Configuration]] to be used only inside the framework.
  */
private[app] object ApplicationConfiguration extends ApplicationConfiguration

package it.agilelab.provisioning.spark.workloads.provisioner.app.config

trait ConfigurationModel {
  val PROVISIONER = "provisioner"

  private val networking: String = "networking"
  private val httpServer: String = "httpServer"
  private val interface: String  = "interface"
  private val port: String       = "port"

  val NETWORKING_HTTPSERVER_INTERFACE: String = s"$networking.$httpServer.$interface"
  val NETWORKING_HTTPSERVER_PORT: String      = s"$networking.$httpServer.$port"

}

package it.agilelab.provisioning.spark.workloads.provisioner.app.config

trait ConfigurationModel {
  val PROVISIONER = "provisioner"

  private val networking: String = "networking"
  private val httpServer: String = "httpServer"
  private val interface: String  = "interface"
  private val port: String       = "port"

  val NETWORKING_HTTPSERVER_INTERFACE: String = s"$networking.$httpServer.$interface"
  val NETWORKING_HTTPSERVER_PORT: String      = s"$networking.$httpServer.$port"

  val CLOUDERA_MODE: String  = s"cloudera-mode"
  val SCHEDULER_PROP: String = s"scheduler-prop"

  val HDFS_NAMENODE0: String = "hdfs-nn0"
  val HDFS_NAMENODE1: String = "hdfs-nn1"
  val WEBHDFS_PORT: String   = "webhdfs-port"

  val WEBHDFS_PROTOCOL: String = "webhdfs-protocol"

  val LOGIN_CONTEXT: String = "login-context"

}

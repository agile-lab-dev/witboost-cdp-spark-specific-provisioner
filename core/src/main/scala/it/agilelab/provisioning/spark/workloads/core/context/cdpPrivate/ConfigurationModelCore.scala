package it.agilelab.provisioning.spark.workloads.core.context.cdpPrivate

trait ConfigurationModelCore {
  val PROVISIONER           = "provisioner"
  val LIVY_URL              = s"livy-url"
  val LOGIN_CONTEXT: String = "login-context"

  val HDFS_NAMENODE0: String   = "hdfs-nn0"
  val HDFS_NAMENODE1: String   = "hdfs-nn1"
  val WEBHDFS_PORT: String     = "webhdfs-port"
  val WEBHDFS_PROTOCOL: String = "webhdfs-protocol"

  val USE_KERBEROS_AUTH: String = "use-kerberos-auth"

}

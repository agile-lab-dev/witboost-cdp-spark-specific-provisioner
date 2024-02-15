package it.agilelab.provisioning.spark.workloads.provisioner.quartz.config

trait ConfigurationModel {
  val PROVISIONER = "provisioner"

  val LIVY_URL = s"livy-url"

  val KRB5_CONF_PATH     = s"krb5-conf"
  val KRB_JAAS_CONF_PATH = s"krb-jaas-conf"

  val LIVY_HOST         = s"livy-host"
  val LIVY_PORT         = s"livy-port"
  val CDP_PRIVATE_REALM = s"cdp-realm"

}

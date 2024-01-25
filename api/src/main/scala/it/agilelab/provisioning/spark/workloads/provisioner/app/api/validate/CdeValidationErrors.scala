package it.agilelab.provisioning.spark.workloads.provisioner.app.api.validate

object CdeValidationErrors {
  val CDE_SERVICE_NOT_FOUND: String             = "CDE Service not found"
  val CDE_SERVICE_NOT_RUNNING: String           = "CDE Service exists but is not running"
  val CDE_SERVICE_DELETED: String               = "CDE Service was recently deleted"
  val CDE_VIRTUAL_CLUSTER_NOT_FOUND: String     = "CDE Virtual Cluster not found"
  val CDE_VIRTUAL_CLUSTER_NOT_ACTIVATED: String = "CDE Virtual Cluster exists but is not properly activated"
  val CDE_VIRTUAL_CLUSTER_DELETED: String       = "CDE Virtual Cluster was recently deleted"

}

package it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.workload

import com.cloudera.cdp.de.model.{ ServiceDescription, VcDescription }
import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.base.Job
import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.request.{ CreateResourceReq, UploadFileReq }

final case class SparkCdpPrivateWorkload(
  domain: String,
  dataProduct: String,
  queue: String,
  job: Job
)

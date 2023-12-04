package it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.workload

import com.cloudera.cdp.de.model.{ ServiceDescription, VcDescription }
import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.base.Job
import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.request.{ CreateResourceReq, UploadFileReq }

final case class SparkCdeWorkload(
  domain: String,
  dataProduct: String,
  service: ServiceDescription,
  vc: VcDescription,
  resources: Seq[CreateResourceReq],
  artifacts: Seq[UploadFileReq],
  job: Job
)

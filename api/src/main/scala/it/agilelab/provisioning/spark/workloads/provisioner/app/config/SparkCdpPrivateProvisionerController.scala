package it.agilelab.provisioning.spark.workloads.provisioner.app.config

import io.circe.generic.auto._
import it.agilelab.provisioning.commons.config.Conf
import it.agilelab.provisioning.commons.principalsmapping.CdpIamPrincipals
import it.agilelab.provisioning.commons.validator.Validator
import it.agilelab.provisioning.mesh.self.service.api.controller.ProvisionerController
import it.agilelab.provisioning.mesh.self.service.api.model.ProvisionRequest
import it.agilelab.provisioning.mesh.self.service.core.provisioner.Provisioner
import it.agilelab.provisioning.spark.workload.core.context.ContextError
import it.agilelab.provisioning.spark.workload.core.models.DpCdp
import it.agilelab.provisioning.spark.workload.core.{ SparkCdpPrivate, SparkWorkloadResponse }
import it.agilelab.provisioning.spark.workloads.provisioner.app.api.context.CdpPrivateValidatorContext
import it.agilelab.provisioning.spark.workloads.provisioner.app.api.validate.SparkCdpPrivateValidator
import it.agilelab.provisioning.spark.workloads.provisioner.quartz.SchedulingServiceWithQuartz
import it.agilelab.provisioning.spark.workloads.provisioner.service.context.ProvisionerContextCdpPrivate
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.CdpPrivateSparkWorkloadGateway
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.mapper.SparkCdpPrivateWorkloadMapper
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.workload.SparkCdpPrivateWorkloadGateway
import it.agilelab.provisioning.spark.workloads.provisioner.service.repository.MemoryStateRepository

object SparkCdpPrivateProvisionerController
    extends ProvisionerControllerFactory[DpCdp, SparkCdpPrivate, CdpIamPrincipals] {
  override def initValidator(conf: Conf): Either[ContextError, Validator[ProvisionRequest[DpCdp, SparkCdpPrivate]]] =
    CdpPrivateValidatorContext.init(conf).map { _ =>
      SparkCdpPrivateValidator.validator()
    }

  override def initProvisioner(
    conf: Conf
  ): Either[ContextError, ProvisionerController[DpCdp, SparkCdpPrivate, CdpIamPrincipals]] = {

    val propFile = ApplicationConfiguration.provisionerConfig.getString(ApplicationConfiguration.SCHEDULER_PROP)

    val propOption        = if (propFile.nonEmpty) Some(propFile) else None
    val schedulingService = new SchedulingServiceWithQuartz(propOption)

    schedulingService.startScheduler()

    ProvisionerContextCdpPrivate.init(conf).map { _ =>
      ProvisionerController.defaultNoAclWithAudit[DpCdp, SparkCdpPrivate](
        SparkCdpPrivateValidator.validator(),
        Provisioner.defaultSync[DpCdp, SparkCdpPrivate, SparkWorkloadResponse, CdpIamPrincipals](
          new CdpPrivateSparkWorkloadGateway(
            new SparkCdpPrivateWorkloadMapper(),
            new SparkCdpPrivateWorkloadGateway(schedulingService)
          )
        ),
        new MemoryStateRepository
      )
    }
  }
}

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
import it.agilelab.provisioning.spark.workload.core.{ SparkCde, SparkWorkloadResponse }
import it.agilelab.provisioning.spark.workloads.provisioner.app.api.context.CdeValidatorContext
import it.agilelab.provisioning.spark.workloads.provisioner.app.api.validate.SparkCdeValidator
import it.agilelab.provisioning.spark.workloads.provisioner.service.context.ProvisionerContextCde
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.CdeSparkWorkloadGateway
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.mapper.SparkCdeWorkloadMapper
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.workload.SparkCdeWorkloadGateway
import it.agilelab.provisioning.spark.workloads.provisioner.service.repository.MemoryStateRepository

object SparkCdeProvisionerController extends ProvisionerControllerFactory[DpCdp, SparkCde, CdpIamPrincipals] {
  override def initValidator(conf: Conf): Either[ContextError, Validator[ProvisionRequest[DpCdp, SparkCde]]] =
    CdeValidatorContext.init(conf).map { ctx =>
      SparkCdeValidator.validator(ctx.cdpDeClient, ctx.s3Gateway)
    }

  override def initProvisioner(
    conf: Conf
  ): Either[ContextError, ProvisionerController[DpCdp, SparkCde, CdpIamPrincipals]] =
    for {
      sparkCdeValidator <- initValidator(conf)
      controller        <- ProvisionerContextCde
                             .init(conf)
                             .map { ctx =>
                               ProvisionerController.defaultNoAclWithAudit[DpCdp, SparkCde](
                                 sparkCdeValidator,
                                 Provisioner.defaultSync[DpCdp, SparkCde, SparkWorkloadResponse, CdpIamPrincipals](
                                   new CdeSparkWorkloadGateway(
                                     new SparkCdeWorkloadMapper(
                                       ctx.cdpDeClient,
                                       ctx.idBrokerHostsProvider,
                                       ctx.artifactoryGateway
                                     ),
                                     new SparkCdeWorkloadGateway(ctx.cdeClusterClientFactory)
                                   )
                                 ),
                                 // TODO we should create our custom controller to avoid to inject a state repo
                                 new MemoryStateRepository
                               )
                             }
    } yield controller
}

package it.agilelab.provisioning.spark.workloads.provisioner.app.config

import it.agilelab.provisioning.commons.config.Conf
import it.agilelab.provisioning.commons.principalsmapping.CdpIamPrincipals
import it.agilelab.provisioning.commons.validator.Validator
import it.agilelab.provisioning.mesh.self.service.api.controller.ProvisionerController
import it.agilelab.provisioning.mesh.self.service.api.model.ProvisionRequest
import it.agilelab.provisioning.spark.workloads.core.context.ContextError

trait ProvisionerControllerFactory[DP_SPEC, COMPONENT_SPEC, PRINCIPAL <: CdpIamPrincipals] {
  def initValidator(conf: Conf): Either[ContextError, Validator[ProvisionRequest[DP_SPEC, COMPONENT_SPEC]]]
  def initProvisioner(conf: Conf): Either[ContextError, ProvisionerController[DP_SPEC, COMPONENT_SPEC, PRINCIPAL]]

  def apply(conf: Conf): Either[ContextError, ProvisionerController[DP_SPEC, COMPONENT_SPEC, PRINCIPAL]] = for {
    validator  <- initValidator(conf)
    controller <- initProvisioner(conf)
  } yield controller
}

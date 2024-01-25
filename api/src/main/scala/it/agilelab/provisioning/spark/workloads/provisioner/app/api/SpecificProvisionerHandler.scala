package it.agilelab.provisioning.spark.workloads.provisioner.app.api

import cats.effect.IO
import io.circe.generic.auto._
import it.agilelab.provisioning.api.generated.definitions.{
  ErrorMoreInfo,
  ProvisioningRequest,
  RequestValidationError,
  ReverseProvisioningRequest,
  SystemError,
  UpdateAclRequest,
  ValidationResult
}
import it.agilelab.provisioning.api.generated.{ Handler, Resource }
import it.agilelab.provisioning.commons.identifier.DefaultIDGenerator
import it.agilelab.provisioning.commons.principalsmapping.CdpIamPrincipals
import it.agilelab.provisioning.mesh.self.service.api.controller.ProvisionerController
import it.agilelab.provisioning.mesh.self.service.api.model.ApiResponse.Status
import it.agilelab.provisioning.mesh.self.service.api.model.{ ApiError, ApiRequest, ApiResponse }
import it.agilelab.provisioning.spark.workload.core.SparkCde
import it.agilelab.provisioning.spark.workload.core.models.DpCdp
import it.agilelab.provisioning.spark.workloads.provisioner.app.api.mapping.{
  ProvisioningStatusMapper,
  ValidationErrorMapper
}
import it.agilelab.provisioning.spark.workloads.provisioner.app.api.validate.CdeValidationErrors

class SpecificProvisionerHandler(
  provisioner: ProvisionerController[DpCdp, SparkCde, CdpIamPrincipals]
) extends Handler[IO] {

  private val NotImplementedError = SystemError(
    error = "Endpoint not implemented",
    userMessage = Some("The requested feature hasn't been implemented"),
    input = None,
    inputErrorField = None,
    moreInfo = Some(ErrorMoreInfo(problems = Vector("Endpoint not implemented"), solutions = Vector.empty))
  )

  private val commonSkipUnprovisionErrors = Set(
    CdeValidationErrors.CDE_SERVICE_NOT_FOUND,
    CdeValidationErrors.CDE_SERVICE_NOT_RUNNING,
    CdeValidationErrors.CDE_SERVICE_DELETED,
    CdeValidationErrors.CDE_VIRTUAL_CLUSTER_NOT_FOUND,
    CdeValidationErrors.CDE_VIRTUAL_CLUSTER_NOT_ACTIVATED,
    CdeValidationErrors.CDE_VIRTUAL_CLUSTER_DELETED
  )

  override def provision(respond: Resource.ProvisionResponse.type)(
    body: ProvisioningRequest
  ): IO[Resource.ProvisionResponse] = IO {
    provisioner.provision(ApiRequest.ProvisioningRequest(body.descriptor)) match {
      case Left(error: ApiError.ValidationError) =>
        Resource.ProvisionResponse.BadRequest(RequestValidationError(error.errors.toVector))
      case Left(error: ApiError.SystemError)     =>
        Resource.ProvisionResponse.InternalServerError(SystemError(error.error))
      case Right(status)                         => Resource.ProvisionResponse.Ok(ProvisioningStatusMapper.from(status))
    }
  }

  override def runReverseProvisioning(
    respond: Resource.RunReverseProvisioningResponse.type
  )(body: ReverseProvisioningRequest): IO[Resource.RunReverseProvisioningResponse] = IO {
    Resource.RunReverseProvisioningResponse.InternalServerError(NotImplementedError)
  }

  override def unprovision(respond: Resource.UnprovisionResponse.type)(
    body: ProvisioningRequest
  ): IO[Resource.UnprovisionResponse] = IO {
    provisioner.unprovision(ApiRequest.ProvisioningRequest(body.descriptor)) match {

      case Left(error: ApiError.ValidationError) if shouldSkipUnprovision(error) =>
        val id     = new DefaultIDGenerator().random()
        val status =
          ApiResponse.ProvisioningStatus(id, Status.COMPLETED, Some("Unprovision skipped. Errors:" + error.errors))
        Resource.UnprovisionResponse.Ok(ProvisioningStatusMapper.from(status))

      case Left(error: ApiError.ValidationError) =>
        Resource.UnprovisionResponse.BadRequest(RequestValidationError(error.errors.toVector))

      case Left(error: ApiError.SystemError) =>
        Resource.UnprovisionResponse.InternalServerError(SystemError(error.error))

      case Right(status) =>
        Resource.UnprovisionResponse.Ok(ProvisioningStatusMapper.from(status))
    }
  }

  override def updateacl(respond: Resource.UpdateaclResponse.type)(
    body: UpdateAclRequest
  ): IO[Resource.UpdateaclResponse] = IO {
    Resource.UpdateaclResponse.InternalServerError(NotImplementedError)
  }

  override def validate(respond: Resource.ValidateResponse.type)(
    body: ProvisioningRequest
  ): IO[Resource.ValidateResponse] = IO {
    provisioner.validate(ApiRequest.ProvisioningRequest(body.descriptor)) match {
      case Left(error: ApiError.SystemError) =>
        Resource.ValidateResponse.InternalServerError(SystemError(error.error))
      case Right(result)                     =>
        Resource.ValidateResponse.Ok(ValidationResult(result.valid, result.error.map(ValidationErrorMapper.from)))
    }
  }

  override def asyncValidate(respond: Resource.AsyncValidateResponse.type)(
    body: ProvisioningRequest
  ): IO[Resource.AsyncValidateResponse] = IO {
    Resource.AsyncValidateResponse.InternalServerError(NotImplementedError)
  }

  override def getValidationStatus(respond: Resource.GetValidationStatusResponse.type)(
    token: String
  ): IO[Resource.GetValidationStatusResponse] = IO {
    Resource.GetValidationStatusResponse.InternalServerError(NotImplementedError)
  }

  override def getStatus(respond: Resource.GetStatusResponse.type)(token: String): IO[Resource.GetStatusResponse] = IO {
    Resource.GetStatusResponse.InternalServerError(NotImplementedError)
  }

  override def getReverseProvisioningStatus(respond: Resource.GetReverseProvisioningStatusResponse.type)(
    token: String
  ): IO[Resource.GetReverseProvisioningStatusResponse] = IO {
    Resource.GetReverseProvisioningStatusResponse.InternalServerError(NotImplementedError)
  }

  private def shouldSkipUnprovision(error: ApiError.ValidationError): Boolean =
    commonSkipUnprovisionErrors.intersect(error.errors.toSet).nonEmpty
}

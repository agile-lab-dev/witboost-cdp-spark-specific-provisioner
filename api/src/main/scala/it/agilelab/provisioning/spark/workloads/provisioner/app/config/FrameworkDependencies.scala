package it.agilelab.provisioning.spark.workloads.provisioner.app.config

import cats.data.Kleisli
import cats.effect.IO
import cats.implicits.toSemigroupKOps
import it.agilelab.provisioning.api.generated.{ Handler, Resource }
import it.agilelab.provisioning.commons.principalsmapping.CdpIamPrincipals
import it.agilelab.provisioning.mesh.self.service.api.controller.ProvisionerController
import it.agilelab.provisioning.spark.workloads.core.models.DpCdp
import it.agilelab.provisioning.spark.workloads.provisioner.app.routes.HealthCheck
import org.http4s.server.middleware.Logger
import org.http4s.{ HttpRoutes, Request, Response }

trait FrameworkDependencies[COMPONENT_SPEC] {
  def provisionerController: ProvisionerController[DpCdp, COMPONENT_SPEC, CdpIamPrincipals]
  protected def createProvisionerHandler(
    provisionerController: ProvisionerController[DpCdp, COMPONENT_SPEC, CdpIamPrincipals]
  ): Handler[IO]
  private def createProvisionerService(handler: Handler[IO]): HttpRoutes[IO]      = new Resource[IO]().routes(handler)
  private def combineServices(provisionerService: HttpRoutes[IO]): HttpRoutes[IO] =
    HealthCheck.routes[IO]() <+> provisionerService
  private def withLogger(service: HttpRoutes[IO]): HttpRoutes[IO]                 = Logger.httpRoutes[IO](
    logHeaders = false,
    logBody = true,
    redactHeadersWhen = _ => false,
    logAction = None
  )(service)

  val httpApp: Kleisli[IO, Request[IO], Response[IO]] = withLogger(
    combineServices(createProvisionerService(createProvisionerHandler(provisionerController)))
  ).orNotFound
}

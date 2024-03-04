package it.agilelab.provisioning.spark.workloads.provisioner.app

import cats.effect.{ ExitCode, IO, IOApp }
import com.comcast.ip4s.{ Host, Port }
import com.typesafe.scalalogging.Logger
import it.agilelab.provisioning.commons.config.Conf

import it.agilelab.provisioning.spark.workloads.core.context.ContextError
import it.agilelab.provisioning.spark.workloads.core.context.ContextError._
import it.agilelab.provisioning.spark.workloads.provisioner.app.config.{
  ApplicationConfiguration,
  FrameworkDependencies,
  FrameworkDependenciesCde,
  FrameworkDependenciesCdpPrivate,
  SparkCdeProvisionerController,
  SparkCdpPrivateProvisionerController
}
import org.http4s.ember.server.EmberServerBuilder

object Main extends IOApp {

  private val logger     = Logger(getClass.getName)
  private val conf: Conf = Conf.envWithAudit()
  override def run(args: List[String]): IO[ExitCode] = {

    val cloudera_mode = ApplicationConfiguration.provisionerConfig.getString(ApplicationConfiguration.CLOUDERA_MODE)

    chooseFrameworkDependenciesWithProvisioner(cloudera_mode).flatMap { case frameworkDependencies =>
      for {
        interface <- IO.fromOption(
                       Host.fromString(
                         ApplicationConfiguration.provisionerConfig.getString(
                           ApplicationConfiguration.NETWORKING_HTTPSERVER_INTERFACE
                         )
                       )
                     )(new RuntimeException("Interface not valid"))
        port      <-
          IO.fromOption(
            Port.fromInt(
              ApplicationConfiguration.provisionerConfig.getInt(ApplicationConfiguration.NETWORKING_HTTPSERVER_PORT)
            )
          )(new RuntimeException("Port not valid"))
        server    <- EmberServerBuilder
                       .default[IO]
                       .withPort(port)
                       .withHost(interface)
                       .withHttpApp(frameworkDependencies.httpApp)
                       .build
                       .useForever
                       .as(ExitCode.Success)
      } yield server
    }.handleErrorWith { error =>
      logger.error("An error occurred during server initialization", error)
      IO.pure(ExitCode.Error)
    }
  }

  private def logError(error: ContextError): Unit =
    error match {
      case confErr: ConfigurationError => logger.error(confErr.toString)
      case clientErr: ClientError      => logger.error(clientErr.client, clientErr.throwable)
    }

  def chooseFrameworkDependenciesWithProvisioner(mode: String): IO[FrameworkDependencies[_]] =
    mode match {
      case "public"  =>
        SparkCdeProvisionerController.apply(conf) match {
          case Left(error: ClientError)        =>
            logError(error)
            IO.raiseError(error.throwable)
          case Left(error: ConfigurationError) =>
            logError(error)
            IO.raiseError(error.error)
          case Right(provisionerController)    =>
            IO.pure((new FrameworkDependenciesCde(provisionerController)))
        }
      case "private" =>
        SparkCdpPrivateProvisionerController.apply(conf) match {
          case Left(error: ClientError)        =>
            logError(error)
            IO.raiseError(error.throwable)
          case Left(error: ConfigurationError) =>
            logError(error)
            IO.raiseError(error.error)
          case Right(provisionerController)    =>
            IO.pure((new FrameworkDependenciesCdpPrivate(provisionerController)))
        }
      case _         => IO.raiseError(new IllegalArgumentException(s"Invalid mode: $mode"))
    }

}

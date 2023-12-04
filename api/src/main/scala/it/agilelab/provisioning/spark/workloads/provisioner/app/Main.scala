package it.agilelab.provisioning.spark.workloads.provisioner.app

import cats.effect.{ ExitCode, IO, IOApp }
import com.comcast.ip4s.{ Host, Port }
import com.typesafe.scalalogging.Logger
import it.agilelab.provisioning.commons.config.Conf
import it.agilelab.provisioning.spark.workload.core.context.ContextError
import it.agilelab.provisioning.spark.workloads.provisioner.app.config.{
  ApplicationConfiguration,
  FrameworkDependencies,
  SparkProvisionerController
}
import org.http4s.ember.server.EmberServerBuilder
import it.agilelab.provisioning.spark.workload.core.context.ContextError._

object Main extends IOApp {

  private val logger     = Logger(getClass.getName)
  private val conf: Conf = Conf.envWithAudit()

  override def run(args: List[String]): IO[ExitCode] = for {
    provisionerController <- SparkProvisionerController.apply(conf) match {
                               case Left(error: ClientError)        =>
                                 logError(error)
                                 IO.raiseError(error.throwable)
                               case Left(error: ConfigurationError) =>
                                 logError(error)
                                 IO.raiseError(error.error)
                               case Right(value)                    => IO.pure(value)
                             }
    frameworkDependencies <- IO.pure(new FrameworkDependencies(provisionerController))
    interface             <- IO.fromOption(
                               Host
                                 .fromString(
                                   ApplicationConfiguration.provisionerConfig.getString(
                                     ApplicationConfiguration.NETWORKING_HTTPSERVER_INTERFACE
                                   )
                                 )
                             )(new RuntimeException("Interface not valid"))
    port                  <- IO.fromOption(
                               Port
                                 .fromInt(
                                   ApplicationConfiguration.provisionerConfig.getInt(ApplicationConfiguration.NETWORKING_HTTPSERVER_PORT)
                                 )
                             )(new RuntimeException("Port not valid"))
    server                <- EmberServerBuilder
                               .default[IO]
                               .withPort(port)
                               .withHost(interface)
                               .withHttpApp(frameworkDependencies.httpApp)
                               .build
                               .useForever
                               .as(ExitCode.Success)
  } yield server

  private def logError(error: ContextError): Unit =
    error match {
      case confErr: ConfigurationError => logger.error(confErr.toString)
      case clientErr: ClientError      => logger.error(clientErr.client, clientErr.throwable)
    }
}

package it.agilelab.provisioning.spark.workloads.provisioner.app.routes

import cats.Monad
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object HealthCheck {
  def routes[F[_]: Monad](
    healthPath: String = "health"
  ): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] { case GET -> Root / `healthPath` =>
      Ok()
    }
  }
}

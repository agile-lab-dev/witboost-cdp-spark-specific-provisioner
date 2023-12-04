package it.agilelab.provisioning.spark.workloads.provisioner.service.repository

import cats.implicits.toBifunctorOps
import it.agilelab.provisioning.commons.config.Conf
import it.agilelab.provisioning.mesh.repository.RepositoryError._
import it.agilelab.provisioning.mesh.repository.{ Repository, RepositoryError }
import it.agilelab.provisioning.mesh.self.service.lambda.core.model.Domain
class ConfigDomainRepository(conf: Conf) extends Repository[Domain, String, Unit] {
  override def findById(id: String): Either[RepositoryError, Option[Domain]] = for {

    //TODO: create DOMAINS section in config file
    domain <- conf.get(s"DOMAINS.$id").leftMap(e => RepositoryInitErr(e))

  } yield Some(Domain(domain, domain))

  override def findAll(filter: Option[Unit]): Either[RepositoryError, Seq[Domain]] = ???

  override def create(entity: Domain): Either[RepositoryError, Unit] = ???

  override def delete(id: String): Either[RepositoryError, Unit] = ???

  override def update(entity: Domain): Either[RepositoryError, Unit] = ???
}

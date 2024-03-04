package it.agilelab.provisioning.spark.workloads.provisioner.app.api.validate

import cats.implicits.{ catsSyntaxEq, toTraverseOps }
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.parser.CronParser
import it.agilelab.provisioning.aws.s3.gateway.S3Gateway
import it.agilelab.provisioning.commons.client.cdp.de.CdpDeClient
import it.agilelab.provisioning.commons.validator.Validator
import it.agilelab.provisioning.mesh.self.service.api.model.Component.Workload
import it.agilelab.provisioning.mesh.self.service.api.model.{ DataProduct, ProvisionRequest }
import it.agilelab.provisioning.spark.workloads.core.SparkCde
import it.agilelab.provisioning.spark.workloads.core.SparkCde._
import it.agilelab.provisioning.spark.workloads.core.models.DpCdp

import java.time.format.DateTimeFormatter
import scala.util.Try

object SparkCdeValidator {

  private val cronParser: CronParser = new CronParser(
    CronDefinitionBuilder
      .defineCron()
      .withMinutes()
      .and()
      .withHours()
      .and()
      .withDayOfMonth()
      .and()
      .withMonth()
      .withValidRange(1, 12)
      .and()
      .withDayOfWeek()
      .withValidRange(0, 6)
      .withMondayDoWValue(0)
      .and()
      .instance()
  )

  private val ALLOWED_PYTHON_VERSIONS = Seq("python2", "python3")

  def validator(
    cdpDeClient: CdpDeClient,
    s3Gateway: S3Gateway
  ): Validator[ProvisionRequest[DpCdp, SparkCde]] = {
    Validator[ProvisionRequest[DpCdp, SparkCde]]
      .rule(
        r => withinReq(r)((_, _) => true),
        _ => "The provided component is not accepted by this provisioner"
      )
      .rule(
        r =>
          withinReq(r) { (_, w) =>
            cdpDeClient
              .findServiceByName(w.specific.cdeService)
              .exists(_.isDefined)
          },
        _ => CdeValidationErrors.CDE_SERVICE_NOT_FOUND
      )
      .rule(
        r =>
          withinReq(r) { (_, w) =>
            cdpDeClient
              .findServiceByName(w.specific.cdeService)
              .map(serviceOption =>
                !(serviceOption.exists(_.getStatus != "ClusterCreationCompleted")
                  && serviceOption.exists(_.getStatus != "ClusterDeletionCompleted"))
              )
              .getOrElse(true)
          },
        _ => CdeValidationErrors.CDE_SERVICE_NOT_RUNNING
      )
      .rule(
        r =>
          withinReq(r) { (_, w) =>
            cdpDeClient
              .findServiceByName(w.specific.cdeService)
              .map(serviceOption =>
                !serviceOption.exists(_.getStatus == "ClusterDeletionCompleted")
              ) //error only if exists and was recently deleted"
              .getOrElse(true)
          },
        _ => CdeValidationErrors.CDE_SERVICE_DELETED
      )
      .rule(
        r =>
          withinReq(r) { (_, w) =>
            cdpDeClient
              .findServiceByName(w.specific.cdeService)
              .flatMap(
                _.flatMap(s => cdpDeClient.findVcByName(s.getClusterId, w.specific.cdeCluster).sequence).sequence
              )
              .exists(_.isDefined)
          },
        _ => CdeValidationErrors.CDE_VIRTUAL_CLUSTER_NOT_FOUND
      )
      .rule(
        r =>
          withinReq(r) { (_, w) =>
            !cdpDeClient
              .findServiceByName(w.specific.cdeService)
              .flatMap(
                _.flatMap(s => cdpDeClient.findVcByName(s.getClusterId, w.specific.cdeCluster).sequence).sequence
              )
              .exists(item =>
                item.exists(_.getStatus != "AppInstalled")
                  && item.exists(_.getStatus != "AppDeleted")
              )
          },
        _ => CdeValidationErrors.CDE_VIRTUAL_CLUSTER_NOT_ACTIVATED
      )
      .rule(
        r =>
          withinReq(r) { (_, w) =>
            !cdpDeClient
              .findServiceByName(w.specific.cdeService)
              .flatMap(
                _.flatMap(s => cdpDeClient.findVcByName(s.getClusterId, w.specific.cdeCluster).sequence).sequence
              )
              .exists(_.exists(_.getStatus == "AppDeleted"))
          },
        _ => CdeValidationErrors.CDE_VIRTUAL_CLUSTER_DELETED
      )
      .rule(
        r =>
          withinReq(r) { case (dp, Workload(s"urn:dmb:cmp:$id", _, _, _, specific)) =>
            sanitise(s"$id-${dp.environment}") === specific.jobName
          },
        _ => "Job name not valid"
      )
      .rule(
        r => withinReq(r)((_, w) => s3pathExists(s3Gateway, w.specific.appFile)),
        _ => "Job Source application file not found"
      )
      .rule(
        r =>
          withinReq(r)((_, w) =>
            w.specific.jobConfig.forall(_.dependencies.forall(_.forall(s3pathExists(s3Gateway, _))))
          ),
        _ => "Job Source dependencies not founded"
      )
      .rule(
        r => withinReq(r)((_, w) => w.specific.entryPoint.forall(_.nonEmpty)),
        _ => "Spark job class name must be non empty"
      )
      .rule(
        r => withinReq(r)((_, w) => w.specific.pyVersion.forall(ALLOWED_PYTHON_VERSIONS.contains)),
        _ => "Python version must be one of \"python2\", \"python3\""
      )
      .rule(
        r =>
          withinReq(r) {
            case (_, Workload(_, _, _, _, PySparkCdeJobWithPyEnv(_, _, _, _, _, rf, pm, _))) =>
              rf.endsWith("requirements.txt") && s3pathExists(s3Gateway, rf) && pm.forall(_.nonEmpty)
            case _                                                                           => true
          },
        _ => "Invalid Python environment settings: settings must be non empty strings which point to existing files; python requirements must be named as \"requirements.txt\""
      )
      .rule(
        r => withinReq(r)((_, w) => w.specific.jobConfig.forall(_.driverCores.forall(_ > 0))),
        _ => "If specified, driver cores must be a positive integer"
      )
      .rule(
        r => withinReq(r)((_, w) => w.specific.jobConfig.forall(_.driverMemory.forall(_.matches("\\d+g")))),
        _ => "If specified, driver memory must be a positive integer followed by 'g' (e.g. 2g)"
      )
      .rule(
        r => withinReq(r)((_, w) => w.specific.jobConfig.forall(_.executorCores.forall(_ > 0))),
        _ => "If specified, executor cores must be a positive integer"
      )
      .rule(
        r => withinReq(r)((_, w) => w.specific.jobConfig.forall(_.executorMemory.forall(_.matches("\\d+g")))),
        _ => "If specified, executor memory must be a positive integer followed by 'g' (e.g. 2g)"
      )
      .rule(
        r => withinReq(r)((_, w) => w.specific.jobConfig.forall(_.numExecutors.forall(_ > 0))),
        _ => "If specified, num executors must be a positive integer"
      )
      .rule(
        r =>
          withinReq(r)((_, w) =>
            w.specific.jobConfig
              .forall(_.schedule.forall(s => Try(cronParser.parse(s.cronExpression)).toEither.isRight))
          ),
        _ => "Invalid cron expression"
      )
      .rule(
        r =>
          withinReq(r)((_, w) =>
            w.specific.jobConfig.forall(_.schedule.forall(s => validateDates(s.startDate, s.endDate)))
          ),
        _ => "startDate, endDate have to match the following pattern: \"yyyy-MM-ddTHH:mm:ssZ\""
      )
  }

  private def withinReq(
    provisionRequest: ProvisionRequest[DpCdp, SparkCde]
  )(func: (DataProduct[DpCdp], Workload[SparkCde]) => Boolean): Boolean = provisionRequest match {
    case ProvisionRequest(dp: DataProduct[DpCdp], wl: Option[Workload[SparkCde]]) =>
      val result = wl match {
        case Some(w) =>
          func(dp, w)
        case None    =>
          false
      }
      result
    case _                                                                        =>
      false
  }

  private def s3pathExists(s3Gateway: S3Gateway, path: String): Boolean =
    path match {
      case s"s3://$bucket/$key" =>
        val x = s3Gateway.objectExists(bucket, key).getOrElse(false)
        x
      case _                    =>
        false
    }

  private def validateDates(startDate: String, endDate: String): Boolean = {
    val dateTime: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
    val result                      = for {
      _ <- Try(dateTime.parse(startDate)).toEither
      _ <- Try(dateTime.parse(endDate)).toEither
    } yield ()
    result.isRight
  }

  private def sanitise(value: String): String =
    value.replaceAll("[^a-zA-Z0-9]", "-")

}

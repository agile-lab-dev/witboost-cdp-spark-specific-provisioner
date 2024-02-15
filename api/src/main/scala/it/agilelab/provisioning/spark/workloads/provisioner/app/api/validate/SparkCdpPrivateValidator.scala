package it.agilelab.provisioning.spark.workloads.provisioner.app.api.validate

import cats.implicits.catsSyntaxEq
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.parser.CronParser
import it.agilelab.provisioning.commons.validator.Validator
import it.agilelab.provisioning.mesh.self.service.api.model.Component.Workload
import it.agilelab.provisioning.mesh.self.service.api.model.{ DataProduct, ProvisionRequest }
import it.agilelab.provisioning.spark.workload.core.SparkCdpPrivate
import it.agilelab.provisioning.spark.workload.core.models.DpCdp
import org.quartz.CronExpression

import java.time.format.DateTimeFormatter
import scala.util.Try

object SparkCdpPrivateValidator {
  def validator(
  ): Validator[ProvisionRequest[DpCdp, SparkCdpPrivate]] =
    Validator[ProvisionRequest[DpCdp, SparkCdpPrivate]]
      .rule(
        r => withinReq(r)((_, _) => true),
        _ => "The provided component is not accepted by this provisioner"
      )
      .rule(
        r =>
          withinReq(r) { case (dp, Workload(s"urn:dmb:cmp:$id", _, _, _, specific)) =>
            sanitise(s"$id-${dp.environment}") === specific.jobName
          },
        _ => "Job name not valid"
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
              .forall(_.schedule.forall(s => isValidCronExpression(s.cronExpression)))
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

  private def withinReq(
    provisionRequest: ProvisionRequest[DpCdp, SparkCdpPrivate]
  )(func: (DataProduct[DpCdp], Workload[SparkCdpPrivate]) => Boolean): Boolean = provisionRequest match {
    case ProvisionRequest(dp: DataProduct[DpCdp], wl: Option[Workload[SparkCdpPrivate]]) =>
      val result = wl match {
        case Some(w) =>
          func(dp, w)
        case None    =>
          false
      }
      result
    case _                                                                               =>
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

  private def isValidCronExpression(cronExpression: String): Boolean =
    try {
      new CronExpression(cronExpression)
      true
    } catch {
      case e: Exception =>
        false
    }

}

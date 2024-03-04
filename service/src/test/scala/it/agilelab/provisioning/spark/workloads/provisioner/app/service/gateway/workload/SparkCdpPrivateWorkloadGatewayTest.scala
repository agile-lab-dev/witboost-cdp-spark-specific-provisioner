package it.agilelab.provisioning.spark.workloads.provisioner.app.service.gateway.workload
import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.base._
import it.agilelab.provisioning.spark.workloads.core.SparkWorkloadResponse
import it.agilelab.provisioning.spark.workloads.provisioner.quartz.{ SchedulerError, SchedulingService }
import it.agilelab.provisioning.spark.workloads.provisioner.service.gateway.workload.{
  SparkCdpPrivateWorkload,
  SparkCdpPrivateWorkloadGateway
}
import org.scalamock.scalatest.MockFactory
import org.scalatest.EitherValues
import org.scalatest.funsuite.AnyFunSuite

import java.util.Date

class SparkCdpPrivateWorkloadGatewayTest extends AnyFunSuite with MockFactory with EitherValues {

  val schedulingService = mock[SchedulingService]
  val sparkJobDeployer  = new SparkCdpPrivateWorkloadGateway(schedulingService)

  test("deployJob should return Right with SparkWorkloadResponse on successful scheduling") {

    val sparkCdpPrivateWorkload = SparkCdpPrivateWorkload(
      "my-dp-domain",
      "my-dp-name",
      "default",
      Job.spark(
        "y",
        "y",
        "z",
        "cn",
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        Some(
          Schedule(
            enabled = true,
            None,
            None,
            None,
            None,
            None,
            start = Some("2024-02-06T14:36:00Z"),
            end = Some("2025-02-06T14:36:00Z"),
            Some("* * * * * ? *"),
            None
          )
        )
      )
    )

    val dateRes = new Date()

    (schedulingService.scheduleJob _)
      .expects(*, *, *)
      .returning(Right(dateRes))
      .once()

    val actual = sparkJobDeployer.deployJob(sparkCdpPrivateWorkload)

    val expected = Right(
      SparkWorkloadResponse(
        Some(
          JobDetails(
            name = "y",
            `type` = "spark",
            created = dateRes.toString,
            modified = dateRes.toString,
            lastUsed = dateRes.toString,
            mounts = Seq(Mount("y")),
            retentionPolicy = "keep_indefinitely",
            spark = Some(
              SparkJob.defaultSparkJob(
                file = "z",
                className = "cn"
              )
            ),
            schedule = Some(
              Schedule(
                enabled = true,
                None,
                None,
                None,
                None,
                None,
                start = Some("2024-02-06T14:36:00Z"),
                end = Some("2025-02-06T14:36:00Z"),
                Some("* * * * * ? *"),
                None
              )
            )
          )
        ),
        None
      )
    )
    assert(actual == expected)
  }

  test("deployJob should return Left with ComponentGatewayError on scheduling failure") {
    val sparkCdpPrivateWorkload = SparkCdpPrivateWorkload(
      "my-dp-domain",
      "my-dp-name",
      "default",
      Job.spark(
        "y",
        "y",
        "z",
        "cn",
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        Some(
          Schedule(
            enabled = true,
            None,
            None,
            None,
            None,
            None,
            start = Some("2024-02-06T14:36:00Z"),
            end = Some("2025-02-06T14:36:00Z"),
            Some("* * * * * ? *"),
            None
          )
        )
      )
    )

    val errorMessage = "Scheduling failed due to XYZ reason"
    (schedulingService.scheduleJob _)
      .expects(*, *, *)
      .returning(Left(SchedulerError(errorMessage)))
      .once()

    val actual = sparkJobDeployer.deployJob(sparkCdpPrivateWorkload)

    assert(actual.isLeft)
    assert(actual.left.value.error == errorMessage)
  }

  test("undeploy should return Right with SparkWorkloadResponse on successful unscheduling") {

    val sparkCdpPrivateWorkload = SparkCdpPrivateWorkload(
      "my-dp-domain",
      "my-dp-name",
      "default",
      Job.spark(
        "y",
        "y",
        "z",
        "cn",
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        Some(
          Schedule(
            enabled = true,
            None,
            None,
            None,
            None,
            None,
            start = Some("2024-02-06T14:36:00Z"),
            end = Some("2025-02-06T14:36:00Z"),
            Some("* * * * * ? *"),
            None
          )
        )
      )
    )

    (schedulingService.unscheduleJob _)
      .expects(*, *)
      .returning(Right(true))
      .once()

    val actual = sparkJobDeployer.undeployJob(sparkCdpPrivateWorkload)

    val expected = Right(
      SparkWorkloadResponse(
        None,
        Some(
          Job.spark(
            "y",
            "y",
            "z",
            "cn",
            None,
            None,
            None,
            None,
            None,
            None,
            None,
            None,
            None,
            Some(
              Schedule(
                enabled = true,
                None,
                None,
                None,
                None,
                None,
                start = Some("2024-02-06T14:36:00Z"),
                end = Some("2025-02-06T14:36:00Z"),
                Some("* * * * * ? *"),
                None
              )
            )
          )
        )
      )
    )
    assert(actual == expected)
  }

  test("undeployJob should return Left with ComponentGatewayError on unscheduling failure") {
    val sparkCdpPrivateWorkload = SparkCdpPrivateWorkload(
      "my-dp-domain",
      "my-dp-name",
      "default",
      Job.spark(
        "y",
        "y",
        "z",
        "cn",
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        Some(
          Schedule(
            enabled = true,
            None,
            None,
            None,
            None,
            None,
            start = Some("2024-02-06T14:36:00Z"),
            end = Some("2025-02-06T14:36:00Z"),
            Some("* * * * * ? *"),
            None
          )
        )
      )
    )

    val errorMessage = "Unscheduling failed due to XYZ reason"
    (schedulingService.unscheduleJob _)
      .expects(*, *)
      .returning(Left(SchedulerError(errorMessage)))
      .once()

    val actual = sparkJobDeployer.undeployJob(sparkCdpPrivateWorkload)

    assert(actual.isLeft)
    assert(actual.left.value.error == errorMessage)
  }

  test("mapJobToJobDetails should correctly map Job to JobDetails") {
    val job = Job(
      name = "exampleJob",
      `type` = "spark",
      mounts = Seq(Mount("exampleMount")),
      retentionPolicy = "keep_indefinitely",
      spark = Some(
        SparkJob.defaultSparkJob(
          file = "z",
          className = "cn"
        )
      ),
      airflow = None,
      schedule = Some(
        Schedule(
          enabled = true,
          None,
          None,
          None,
          None,
          None,
          start = Some("2024-02-06T14:36:00Z"),
          end = Some("2025-02-06T14:36:00Z"),
          Some("* * * * * ? *"),
          None
        )
      )
    )

    val date = new Date()

    val jobDetails = sparkJobDeployer.mapJobToJobDetails(job, date)

    assert(jobDetails.name == job.name)
    assert(jobDetails.`type` == job.`type`)
    assert(jobDetails.created == date.toString)
    assert(jobDetails.modified == date.toString)
    assert(jobDetails.lastUsed == date.toString)
    assert(jobDetails.mounts == job.mounts)
    assert(jobDetails.retentionPolicy == job.retentionPolicy)
    assert(jobDetails.spark == job.spark)
    assert(jobDetails.schedule == job.schedule)
  }

}

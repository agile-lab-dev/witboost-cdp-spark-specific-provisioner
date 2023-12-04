package it.agilelab.provisioning.spark.workload.core

import it.agilelab.provisioning.commons.client.cdp.de.cluster.model.base.{ Job, JobDetails, Mount }
import org.scalatest.funsuite.AnyFunSuite

class SparkWorkloadResponseTest extends AnyFunSuite {

  test("create") {
    val actual   = SparkWorkloadResponse.create(
      JobDetails("a", "b", "c", "d", "e", Seq(Mount("f")), "g", None, None)
    )
    val expected = SparkWorkloadResponse(
      Some(JobDetails("a", "b", "c", "d", "e", Seq(Mount("f")), "g", None, None)),
      None
    )
    assert(actual == expected)
  }

  test("destroy") {
    val actual   = SparkWorkloadResponse.destroy(
      Job("a", "b", Seq(Mount("f")), "c", None, None, None)
    )
    val expected = SparkWorkloadResponse(
      None,
      Some(Job("a", "b", Seq(Mount("f")), "c", None, None, None))
    )
    assert(actual == expected)
  }
}

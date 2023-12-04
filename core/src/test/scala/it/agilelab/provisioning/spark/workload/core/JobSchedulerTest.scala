package it.agilelab.provisioning.spark.workload.core

import org.scalatest.funsuite.AnyFunSuite

class JobSchedulerTest extends AnyFunSuite {

  test("create") {
    val actual = JobScheduler.create("a", "b", "c")
    assert(actual == JobScheduler("a", "b", "c"))
  }

}

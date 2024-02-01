package it.agilelab.provisioning.spark.workloads.provisioner.quartz.embedded.scheduler

import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite
import quartz.{ TriggerManager, TriggerManagerError }

import java.util.Date

class TriggerManagerTest extends AnyFunSuite with MockFactory {

  test("TriggerManager should create a trigger successfully") {
    val triggerManager = new TriggerManager()

    val jobName   = "testJob"
    val groupName = "testGroup"
    val interval  = "0/10 * * * * ?"
    val startDate = new Date()
    val endDate   = new Date(System.currentTimeMillis() + 3600 * 1000)

    val result = triggerManager.createTrigger(jobName, groupName, Some(interval), startDate, endDate)

    result match {
      case Left(error)    =>
        fail(s"Failed to create trigger: $error")
      case Right(trigger) =>
        assert(trigger.getKey.getName === s"${jobName}Trigger")
        assert(trigger.getKey.getGroup === groupName)
    }
  }

  test("TriggerManager should handle errors when creating a trigger") {
    val triggerManager = new TriggerManager()

    val jobName   = "testJob"
    val groupName = "testGroup"
    val interval  = "0/10 * * * * ?"
    val startDate = new Date()
    val endDate   = new Date(System.currentTimeMillis() + 3600 * 1000)

    // Forcing an error by providing an invalid job name
    val result = triggerManager.createTrigger("", groupName, Some(interval), startDate, endDate)

    result match {
      case Left(error) =>
        assert(error.isInstanceOf[TriggerManagerError])
      case Right(_)    =>
        fail("Expected an error but trigger creation was successful")
    }
  }
}

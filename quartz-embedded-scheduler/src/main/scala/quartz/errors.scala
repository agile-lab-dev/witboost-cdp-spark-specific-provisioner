package quartz

sealed trait JobManagerError
final case class JobSchedulerError(message: String) extends JobManagerError
final case object JobUnexpectedError                extends JobManagerError

sealed trait TriggerManagerError
final case class TriggerSchedulerError(message: String) extends TriggerManagerError
final case object TriggerUnexpectedError                extends TriggerManagerError

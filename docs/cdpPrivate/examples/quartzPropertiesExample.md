org.quartz.scheduler.instanceName = PostgresScheduler
org.quartz.scheduler.instanceId = AUTO

org.quartz.threadPool.class = org.quartz.simpl.SimpleThreadPool
org.quartz.threadPool.threadCount = 4

org.quartz.jobStore.class = org.quartz.impl.jdbcjobstore.JobStoreTX
org.quartz.jobStore.driverDelegateClass = org.quartz.impl.jdbcjobstore.PostgreSQLDelegate
org.quartz.jobStore.dataSource = quartzDS

org.quartz.dataSource.quartzDS.driver = org.postgresql.Driver
org.quartz.dataSource.quartzDS.URL = jdbc:postgresql://localhost:5432/quartz
org.quartz.dataSource.quartzDS.user = quartzUser
org.quartz.dataSource.quartzDS.password = passwordHere




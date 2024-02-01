# Changelog

All notable changes to this project will be documented in this file.

## v1.0.0

### Commits

- **[WIT-1022] Quartz based embedded workload scheduler**
  > 
  > ##### New features and improvements
  > 
  > Implemented a simple embedded scheduler based on Quartz.
  > Required as input a Job (in the example we provide a Job that calls the Livy API, but any Job that extends https://www.quartz-scheduler.org/api/2.1.7/org/quartz/Job.html is accepted), its name and the name of the group in which it is saved. Optionally also the field for scheduling.
  > The scheduler allows to:
  > - create new quartz jobs
  > - associate triggers with jobs with a cronexpression that indicates how often to execute them
  > - verify the existence of a quartz job
  > - delete quartz jobs
  > 
  > 
  > ##### Related issue
  > 
  > Closes WIT-1022
  > 
  > 

- **[WIT-963] CDP Private Spark SP HLD**
  > 
  > ##### New features and improvements
  > 
  > HLD for CDP private created
  > 
  > ##### Related issue
  > 
  > Closes WIT-963
  > 
  > 

- **[WIT-1172] CDE Services and Clusters are not filtered by status in CDP Spark SP**
  > 
  > ##### Bug fixes
  > Now the provisioner works only with CDE service and cluster in running state.
  > 
  > Created more specific errors based on the status of CDE Services and Virtual Clusters.
  > 
  > The unprovisioning request succeeds even if service or cluster is not active indicating the message "Unprovision skipped".
  > 
  > ##### Related issue
  > Closes WIT-1172
  > 
  > 

- **Resolve WIT-801 "Helm chart for cdp spark sp"**
  > 
  > ##### New features and improvements
  > 
  > This MR adds:
  > 
  > * docker image creation: the build stage of the CI is modified to publish to the docker registry
  > * helm chart for k8s deployment
  > 
  > 

- **[WIT-364] CDP specific provisioner for Spark CDE cleanup and refactoring"**
  > 
  > Closes WIT-364

- **add README**




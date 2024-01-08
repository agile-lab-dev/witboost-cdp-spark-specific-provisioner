<p align="center">
    <a href="https://www.agilelab.it/witboost">
        <img src="docs/img/witboost_logo.svg" alt="witboost" width=600 >
    </a>
</p>  

Designed by [Agile Lab](https://www.agilelab.it/), witboost is a versatile platform that addresses a wide range of sophisticated data engineering challenges. It enables businesses to discover, enhance, and productize their data, fostering the creation of automated data platforms that adhere to the highest standards of data governance. Want to know more about witboost? Check it out [here](https://www.agilelab.it/witboost) or [contact us!](https://www.agilelab.it/contacts)

This repository is part of our [Starter Kit](https://github.com/agile-lab-dev/witboost-starter-kit) meant to showcase witboost's integration capabilities and provide a "batteries-included" product.

# CDP Spark Specific Provisioner

- [Overview](#overview)
- [Building](#building)
- [Running](#running)
- [Configuring](#configuring)
- [Deploying](#deploying)

## Overview

This project implements a simple Specific Provisioner that provision Spark Jobs on a CDP environment.

### What's a Specific Provisioner?

A Specific Provisioner is a microservice which is in charge of deploying components that use a specific technology. When the deployment of a Data Product is triggered, the platform generates it descriptor and orchestrates the deployment of every component contained in the Data Product. For every such component the platform knows which Specific Provisioner is responsible for its deployment, and can thus send a provisioning request with the descriptor to it so that the Specific Provisioner can perform whatever operation is required to fulfill this request and report back the outcome to the platform.


### CDP

Cloudera Data Platform (CDP) is an integrated data management and analytics platform that empowers organizations to efficiently collect, store, and analyze data from various sources. CDP offers a unified environment for data management, enabling businesses to make informed decisions, gain insights, and drive data-driven innovation. Explore CDP further with the official documentation: [Cloudera Data Platform Documentation](https://www.cloudera.com/products/cloudera-data-platform.html).

### Software stack

This microservice is written in Scala 2.13, using HTTP4S for the HTTP layer. Project is built with SBT and supports packaging as JAR, fat-JAR and Docker image, ideal for Kubernetes deployments (which is the preferred option).


## Building

**Requirements:**
- Java 11
- SBT

This project depends on a private library `scala-mesh-commons` which you should have access to at compile time. Currently, the library is hosted as a package in a Gitlab Maven Package Registry.

To pull these libraries, we need to set up authentication to the Package Registry (see [Gitlab docs](https://docs.gitlab.com/ee/user/packages/maven_repository/?tab=sbt)). We've set authentication based on environment variables that sbt uses to authenticate. Please export the following environment variables before importing the project:

```bash
export GITLAB_ARTIFACT_HOST=https://gitlab.com/api/v4/projects/51107980/packages/maven
export GITLAB_ARTIFACT_USER=<Gitlab Username>
export GITLAB_ARTIFACT_TOKEN=<Gitlab Personal Access Token>
```

**Plugins:**  
This project uses the following sbt plugins:

- scalaformat: to keep the scala style aligned with all collaborators
- wartRemover: to keep the code more functional as possible
- scoverage: to create a test coverage report


**Generating sources:** this project uses OpenAPI as standard API specification and the [sbt-guardrail](https://github.com/guardrail-dev/sbt-guardrail) plugin to generate server code from the [specification](./api/src/main/openapi/interface-specification.yml).

The code generation is done automatically in the compile phase:

```bash
sbt compile
```

**Tests:** are handled by the standard task as well:

```bash
sbt test
```

**Artifacts & Docker image:** the project uses SBT Native Packager for packaging. Build artifacts with:

```bash
sbt package
```

To build an image using the local Docker server:

```bash
sbt docker:publishLocal
```

*Note:* the version for the project is automatically computed using the environment variable `VERSION`.

**CI/CD:** the pipeline is based on GitLab CI as that's what we use internally. It's configured by the `.gitlab-ci.yaml` file in the root of the repository. You can use that as a starting point for your customizations.


**Project modules:**  
This is a multi module sbt project:
* **api**: Contains the api logic of the provisioner. The latter can be invoked synchronously in 3 different ways:
  1. POST/provision: provision the spark workload specified in the payload request. It will do the provisioning logic and will return a token that can be used to retrieve the request status.
  2. POST/unprovision: unprovision the spark workload specified in the payload request. It will do the unprovisioning logic and will return a token that can be used to retrieve the request status.
  3. POST/validate: validate the payload request and return a validation result. It should be invoked before provisioning a resource in order to understand if the request is correct.
* **core**: Contains model case classes and shared logic among the projects
* **service**: Contains the Provisioner Service logic. This is the module on which we provision the workload.

See [HLD documentation](docs%2FHDL.md)  for more information about the logic of this project.

## Running

To run the server locally, use:

```bash
sbt compile run
```

By default, the server binds to port 8093 on localhost. After it's up and running you can make provisioning requests to this address.  
In [docs/inputExamples](docs%2FinputExamples) you can find some examples of input requests.

## Configuring

Most application configurations are handled with the Typesafe Config library. You can find the default settings in the `reference.conf`. Customize them and use the `config.file` system property or the other options provided by Typesafe Config according to your needs.

### Environment Variables

Before running the application, you need to set the following environment variables:

```bash
export AWS_REGION=<Your AWS_REGION>
export AWS_ACCESS_KEY_ID=<Your AWS_ACCESS_KEY_ID>
export AWS_SECRET_ACCESS_KEY=<Your AWS_SECRET_ACCESS_KEY>
export AWS_SESSION_TOKEN=<Your AWS_SESSION_TOKEN>
```

```bash
export CDP_ACCESS_KEY_ID=<Your CDP_ACCESS_KEY_ID>
export CDP_PRIVATE_KEY=<Your CDP_PRIVATE_KEY>
```

```bash
export CDP_DEPLOY_ROLE_USER=<Your CDP workload user>
export CDP_DEPLOY_ROLE_PASSWORD=<Your CDP workload password>
```

Note: CDP_ACCESS_KEY_ID and CDP_PRIVATE_KEY can be generated on the CDP in Cloudera Management Console in the User Management section.
CDP_DEPLOY_ROLE_USER and CDP_DEPLOY_ROLE_PASSWORD can be generated in the same tab setting the Workload password.

## Deploying

This microservice is meant to be deployed to a Kubernetes cluster.

## License

This project is available under the [Apache License, Version 2.0](https://opensource.org/licenses/Apache-2.0); see [LICENSE](LICENSE) for full details.

## Tech debt

* Improve code coverage

## About us

<p align="center">
    <a href="https://www.agilelab.it">
        <img src="docs/img/agilelab_logo.svg" alt="Agile Lab" width=600>
    </a>
</p>

Agile Lab creates value for its Clients in data-intensive environments through customizable solutions to establish performance driven processes, sustainable architectures, and automated platforms driven by data governance best practices.

Since 2014 we have implemented 100+ successful Elite Data Engineering initiatives and used that experience to create Witboost: a technology-agnostic, modular platform, that empowers modern enterprises to discover, elevate and productize their data both in traditional environments and on fully compliant Data mesh architectures.

[Contact us](https://www.agilelab.it/contacts) or follow us on:
- [LinkedIn](https://www.linkedin.com/company/agile-lab/)
- [Instagram](https://www.instagram.com/agilelab_official/)
- [YouTube](https://www.youtube.com/channel/UCTWdhr7_4JmZIpZFhMdLzAA)
- [Twitter](https://twitter.com/agile__lab)


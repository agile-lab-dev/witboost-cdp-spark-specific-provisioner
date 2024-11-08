<br/>
<p align="center">
    <a href="https://www.witboost.com/">
        <img src="docs/img/witboost_logo.svg" alt="witboost" width=600 >
    </a>
</p>  
<br/>

Designed by [Agile Lab](https://www.agilelab.it/), Witboost is a versatile platform that addresses a wide range of sophisticated data engineering challenges. It enables businesses to discover, enhance, and productize their data, fostering the creation of automated data platforms that adhere to the highest standards of data governance. Want to know more about Witboost? Check it out [here](https://www.witboost.com/) or [contact us!](https://witboost.com/contact-us)

This repository is part of our [Starter Kit](https://github.com/agile-lab-dev/witboost-starter-kit) meant to showcase Witboost's integration capabilities and provide a "batteries-included" product.

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

This project also depends on Witboost library [scala-mesh-commons](https://github.com/agile-lab-dev/witboost-scala-mesh-commons), published Open-Source on Maven Central.

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

See [CDP public HLD documentation](docs/cdpPublic/HLD.md) or [CDP private HLD documentation](docs/cdpPrivate/HLD.md) for more information about the logic of this project.

## Running

To run the server locally, use:

```bash
sbt compile run
```

By default, the server binds to port 8093 on localhost. After it's up and running you can make provisioning requests to this address.
In [docs/cdpPublic/inputExamples](docs/cdpPublic/inputExamples) you can find some examples of input requests.

## Configuring

Most application configurations are handled with the Typesafe Config library. You can find the default settings in the `reference.conf`. Customize them and use the `config.file` system property or the other options provided by Typesafe Config according to your needs.

### Environment Variables

If you are using Cloudera Data Platform (CDP) Private Cloud Base follow the instructions in [configuration_private.md](docs%2Fconfiguration_private.md).
If, otherwise, you prefer to configure the provisioner to communicate with Cloudera Data Engeneering (CDE) read the file [configuration_public.md](docs%2Fconfiguration_public.md).


## Deploying

This microservice is meant to be deployed to a Kubernetes cluster.

## License

This project is available under the [Apache License, Version 2.0](https://opensource.org/licenses/Apache-2.0); see [LICENSE](LICENSE) for full details.


## About Witboost

[Witboost](https://witboost.com/) is a cutting-edge Data Experience platform, that streamlines complex data projects across various platforms, enabling seamless data production and consumption. This unified approach empowers you to fully utilize your data without platform-specific hurdles, fostering smoother collaboration across teams.

It seamlessly blends business-relevant information, data governance processes, and IT delivery, ensuring technically sound data projects aligned with strategic objectives. Witboost facilitates data-driven decision-making while maintaining data security, ethics, and regulatory compliance.

Moreover, Witboost maximizes data potential through automation, freeing resources for strategic initiatives. Apply your data for growth, innovation and competitive advantage.

[Contact us](https://witboost.com/contact-us) or follow us on:

- [LinkedIn](https://www.linkedin.com/showcase/witboost/)
- [YouTube](https://www.youtube.com/@witboost-platform)



# Environment Variables

Before running the application, you need to set the following environment variables:

```bash
export CLOUDERA_MODE = public
```

## AWS Credentials

---
```
export AWS_REGION=<Your AWS_REGION>
export AWS_ACCESS_KEY_ID=<Your AWS_ACCESS_KEY_ID>
export AWS_SECRET_ACCESS_KEY=<Your AWS_SECRET_ACCESS_KEY>
export AWS_SESSION_TOKEN=<Your AWS_SESSION_TOKEN>
```

AWS_REGION: Specifies the AWS region your resources are or will be deployed in.

AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY: These credentials are used to authenticate your application to access AWS services securely.

AWS_SESSION_TOKEN: Required if you are using temporary credentials that are provided by AWS STS (Security Token Service).


## Cloudera Data Platform (CDP) Credentials

---
```bash
export CDP_ACCESS_KEY_ID=<Your CDP_ACCESS_KEY_ID>
export CDP_PRIVATE_KEY=<Your CDP_PRIVATE_KEY>
```
These credentials allow your application to authenticate against Cloudera Data Platform services. They can be generated in the Cloudera Management Console under the User Management section.

```bash
export CDP_DEPLOY_ROLE_USER=<Your CDP workload user>
export CDP_DEPLOY_ROLE_PASSWORD=<Your CDP workload password>
```
These can also be set up in the User Management section of the Cloudera Management Console by setting a Workload password.

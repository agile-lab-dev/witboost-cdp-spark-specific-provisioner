
### Environment Variables

Before running the application, you need to set the following environment variables:

```bash
export CLOUDERA_MODE = public
```

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
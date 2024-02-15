### Environment Variables

Before running the application, you need to set the following environment variables:

```bash
export CLOUDERA_MODE = private
```

```bash
export LIVY_HOST=<Your LIVY_HOST>
export LIVY_URL=<Your LIVY_URL>
export LIVY_PORT=<Your LIVY_PORT>
export CDP_PRIVATE_REALM=<Your default_realm>
```

```bash
export KRB5_CONF_PATH=<Path to your krb5.conf file>
export KRB_JAAS_CONF_PATH=<Path to your kerberos.jaas.conf file>
```

```bash
export SCHEDULER_PROP=<Path to your quartz.properties file>
```

In [quartzPropertiesExample.md](examples%2FquartzPropertiesExample.md) you can find an example of a quartz.properties file to enable the scheduling of jobs on a postgres database.
Update it by following the [official documentation](https://www.quartz-scheduler.org/documentation/quartz-2.1.7/configuration/ConfigDataSources.html).

Database tables can be created using the scripts contained [here](https://github.com/quartz-scheduler/quartz/tree/d42fb7770f287afbf91f6629d90e7698761ad7d8/quartz-core/src/main/resources/org/quartz/impl/jdbcjobstore).



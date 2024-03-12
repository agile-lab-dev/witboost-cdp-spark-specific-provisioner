# Environment Variables

Before running the application, you need to set the following environment variables:

## Cloudera mode

---
```bash
export CLOUDERA_MODE=private
```

## Livy Server URL Configuration

---
```bash
export LIVY_URL=<Your LIVY_URL>
```
The LIVY_URL parameter must be set to point to the endpoint where the Livy server is accessible. This URL varies based on your runtime environment and Livy server setup. Both HTTP and HTTPS protocols can be used, depending on the security configuration of your server. Ensure to replace `<Your LIVY_URL>` with the actual hostname or IP address of your Livy server.

Configuration examples: 
- for HTTP: `http://yourLivyUrl:8998`
- for HTTPS: `https://yourLivyUrl:8998`

**Note**: The port number `8998` is default for Livy servers but may need to be adjusted based on your server's specific configuration.

## Kerberos configuration 

---
1. **Kerberos authentication flag**
- To enable Kerberos authentication, you should set the `USE_KERBEROS_AUTH` to `true`.
- If you do not wish to use Kerberos authentication, set this flag to
   any other value.

   ```bash
   export USE_KERBEROS_AUTH=<true|anyValue>
   ```
   
2. **Kerberos Configuration File Path**: Set the `KRB5_CONF_PATH` variable to point to your Kerberos configuration file (`krb5.conf`). This file contains the Kerberos client settings.

    ```bash
    export KRB5_CONF_PATH=<Path to your krb5.conf file>
    ```

3. **JAAS Configuration File Path**: Specify the location of your Kerberos JAAS configuration file (`kerberos.jaas.conf`) using the `KRB_JAAS_CONF_PATH` variable. This file defines the authentication modules for Java applications.

    ```bash
    export KRB_JAAS_CONF_PATH=<Path to your kerberos.jaas.conf file>
    ```

4. **Login Context Name**: The `LOGIN_CONTEXT` variable should be set to the name of your login context, as defined in your JAAS configuration file.

    ```bash
    export LOGIN_CONTEXT=<Your login context name>
    ```


### Example JAAS Configuration for Kerberos Authentication
A Kerberos JAAS (Java Authentication and Authorization Service) configuration file is used to define the authentication and authorization modules for Java applications. It specifies how these applications should authenticate using Kerberos and other details necessary for secure communication. The structure of this file is critical for setting up Kerberos authentication properly.

Here is a basic example of what entries in a Kerberos JAAS configuration file might look like:

```plaintext
LoginContext {
    com.sun.security.auth.module.Krb5LoginModule required
    useKeyTab=true
    keyTab="/path/to/keytab/file.keytab"
    storeKey=true
    useTicketCache=false
    principal="principalName@REALM.COM";
};
```


## HDFS

---

1. **Name Node Variables**: The HDFS cluster utilizes primary and secondary name nodes to ensure redundancy and failover capabilities. Assign the connection details for these name nodes in your environment with the export commands:

    ```bash
    export HDFS_NAMENODE0=<Your first HDFS name node address>
    export HDFS_NAMENODE1=<Your second HDFS name node address>
    ```

   Replace `<Your first HDFS name node address>` and `<Your second HDFS name node address>` with the actual addresses of your primary and secondary name nodes respectively.

   **Example Configuration**:
    - Primary name node: `HDFS_NAMENODE0=cluster0-vm0.cluster0.example.com`
    - Secondary name node: `HDFS_NAMENODE1=cluster0-vm1.cluster0.example.com`


2. **WebHDFS Port Configuration**

    ```bash
    export WEBHDFS_PORT=<Your WebHDFS port number>
    ```

   Replace `<Your WebHDFS port number>` with the designated port number for WebHDFS in your configuration to allow HTTP access to HDFS data.


3. **WebHDFS Protocol Specification**: To define whether WebHDFS communicates over HTTP or HTTPS, set the `WEBHDFS_PROTOCOL` environment variable accordingly:

    ```bash
    export WEBHDFS_PROTOCOL=<http or https>
    ```

    The value of `WEBHDFS_PROTOCOL` should be either `http` for standard HTTP communication or `https` for secure HTTP communication. This setting ensures that your HDFS interactions meet the security requirements of your environment.



## Quartz scheduler configuration file

---
```bash
export SCHEDULER_PROP=<Path to your quartz.properties file>
```

Replace `<Path to your quartz.properties file>` with the actual path to your configuration file.

### Configuration File Example and Documentation

- An example `quartz.properties` file can be found in [quartzPropertiesExample.md](cdpPrivate%2Fexamples%2FquartzPropertiesExample.md). This example demonstrates how to configure Quartz to schedule jobs on a PostgreSQL database.

- For detailed configuration options and instructions, refer to the [official Quartz documentation](https://www.quartz-scheduler.org/documentation/quartz-2.1.7/configuration/ConfigDataSources.html). 

### Database Tables Setup

- Quartz requires specific database tables for its operation. The scripts to create these tables are available [here](https://github.com/quartz-scheduler/quartz/tree/d42fb7770f287afbf91f6629d90e7698761ad7d8/quartz-core/src/main/resources/org/quartz/impl/jdbcjobstore). Ensure that these tables are created in your database to facilitate the Quartz job scheduling processes.












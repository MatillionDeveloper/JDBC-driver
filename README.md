# JDBC-driver
This is the [JDBC driver](https://documentation.matillion.com/docs/2732018) for [Matillion ETL](https://www.matillion.com/)

## Installing the driver
This driver will only work within a Matillion ETL instance.

For instructions on building your own custom JDBC driver, please see the [JavaDocs](https://matilliondeveloper.github.io/JDBC-driver/)

## Installing the driver into Matillion ETL
Edit your /usr/share/emerald/WEB-INF/classes/jdbc-providers.properties file and add this JSON fragment

```
  {
    "name": "METL",
    "driver": "com.thereisnogravity.Driver",
    "url": "jdbc:metl://",
    "fetchSize": "500",
    "limit": "limit-inline",
    "allowUpload": true
  }
```

Make sure it's valid by running `jq "." < /usr/share/emerald/WEB-INF/classes/jdbc-providers.properties`

Restart your Matillion instance.

Once it's running again, log into the UI and upload the single JAR file following [this guide](https://documentation.matillion.com/docs/2732018#upload-the-jar-files-into-matillion-etl).

## Usage
Use a [Database Query component](https://documentation.matillion.com/docs/2147237) and
* Select METL as the Database Type
* Set the Connection URL to `jdbc:metl://`
* Provide the username and password of a user that's privileged to use the Matillion API

You will find various built-in data sources available, including

* group, project, version and job - standard Matillion metadata
* schedule and environment - covering all projects that the user is permitted to access
* runningjob - showing a snapshot of all running jobs in all projects
* joblaunchstats - gives you insight into any latency issues which are preventing your jobs from starting running immediately after they are launched (for example trying to run too many jobs at once, or trying to run the same named job twice in parallel). The statistics will cycle once per week, so you should aim to capture them to a more permanent table
* instance - giving you basic metrics on your VM, such as CPU, network, timezone etc

## Implementation Examples
See under the examples/ directory.


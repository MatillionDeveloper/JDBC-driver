# LUFC-Driver
This is an example [JDBC driver](https://documentation.matillion.com/docs/2732018) built using the [com.thereisnogravity.jdbcstub](https://matilliondeveloper.github.io/JDBC-driver/) package.

It has almost no functionality other than to act as a demonstration of how to use the library.

## Compiling the driver
Use the three source .java files from src and compile them referencing JDBC-Stub.jar

```
javac -d . -cp JDBC-Stub.jar *.java

echo -n "com.thereisnogravity.lufc.LUFCDriver" > META-INF/services/java.sql.Driver

jar -cvf LUFC-Driver.jar com META-INF/services/

zip -d LUFC-Driver.jar META-INF/MANIFEST.MF
```

## Installing the driver into Matillion ETL
Edit your /usr/share/emerald/WEB-INF/classes/jdbc-providers.properties file and add this JSON fragment

```
  {
    "name": "LUFC Driver",
    "driver": "com.thereisnogravity.lufc.LUFCDriver",
    "url": "jdbc:lufc://",
    "fetchSize": "500",
    "limit": "limit-inline",
    "allowUpload": true
  }
```

Make sure it's valid by running `jq "." < /usr/share/emerald/WEB-INF/classes/jdbc-providers.properties`

Restart your Matillion instance.

Once it's running again, log into the UI and upload both JAR files (JDBC-Stub.jar and LUFC-Driver.jar) following [this guide](https://documentation.matillion.com/docs/2732018#upload-the-jar-files-into-matillion-etl).

## Usage
Use a [Database Query component](https://documentation.matillion.com/docs/2147237) and
* Select LUFC as the Database Type
* Set the Connection URL to `jdbc:lufc://`
* Provide any username and password

You will find one data source available:

* results - a random set of historical match results


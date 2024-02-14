# Export/import tools

In the GridDB export/import tools, to recover a database from local damages or the database migration process, save/recovery functions are provided in the database and container unit.

To more easily handle output data, the export tool switches the output format of row data files according to the container type. There are two types of export containers:
- A container that sequentially accumulates data and thus tends to grow in size is called **date accumulation container**. For this type of export container, row data files are output for each date.
- The rest of containers is called **registration update container**. For this type of export container, row data files are output for each container.

The following table summarizes the features of containers. 

| container       | table partitioning    | partitioning type            | partitioning key         | export container type                                 |
|----------------|--------------------------------|----------------------------------------|--------------------------------|------------------------------------------------|
| collection   | applicable                           | hash partitioning             | Timestamp type<br>other than Timestamp type | registration update container                               |
| collection   | applicable                           | interval partitioning             | Timestamp type<br>other than Timestamp type | date accumulation container<br>registration update container                              |
| collection   | applicable                           | interval hash partitioning             | Timestamp type<br>other than Timestamp type | date accumulation  container<br>registration update container                              |
| collection   | not applicable                           | -             | Timestamp type<br>other than Timestamp type | registration update container                               |
| time series container   | applicable                           | hash partitioning           | Timestamp type<br>other than Timestamp type | registration update container                               |
| time series container   | applicable                           | interval partitioning           | Timestamp type<br>other than Timestamp type | date accumulation  container<br>registration update container                               |
| time series container   | applicable                           | interval hash partitioning           | Timestamp type<br>other than Timestamp type | date accumulation  container<br>registration update container                               |
| time series container   | not applicable                           | -          | - | date accumulation  container                               |

## Installed directories and files

The export tool saves the container and row data of a GridDB cluster in the file below. A specific container can also be exported by specifying its name.
- **Container data file**
  -   Save GridDB container data and row data.
  -   There are 2 types of format available, one for saving data in a container unit and the other for consolidating and saving data in multiple containers.
- **Export execution data file**
  -   Save the data during export execution. This is required to directly recover exported data in a GridDB cluster.

The import tool imports the container and export execution data files, and recover the container and row data in GridDB. A specific container data can also be imported as well.

### Container data files

Container data files are composed of **metadata files** and **row data files**.

A metadata file is a json-format file which contains the container type and schema, and the index data that has been set up.

There are 2 types of row data file, one of which is the **CSV data file** in which container data is stored in the CSV format, and the other is the **binary data file** in which data is stored in a zip format.

- CSV data file:
  -   Stores container row data as CSV data. Readability is high, and the file can be imported and edited with generic tools.
  -   If the row data is a specific data type such as BLOB, spatial data, array etc., the data is stored in an external object file while only the external object file name is stored in a CSV data file. An external object file is created for each row data.
- Binary data file:
  -   Stores container row data in the Zip format. Can be created with the command gs_export only.  Size is smaller compared to a CSV data file. In addition, the number of files can be reduced as there is no need to create external object files. However, binary data files are not readable and cannot be edited.

See [Format of a container data file](#format_of_container_data_file) for details of the contents described in each file.


For **registration update containers**, row data files are output on a per-container basis.
Depending on the number of containers to be listed, there are two types of container data files as shown below:
- Single container configuration: Holds 1 container data file for each container
- Multi-container configuration: Consolidates multiple containers into a single container data file

For **date accumulation containers**, row data files are output on a per-day basis.
- date container configuration: Data in a container is split on a per-day basis, and each day has one container data file. 

|  Export container type           | Output container configuration      | Description |
| ---------------------- | -------------------- | ------|
| registration update container       | single container configuration  | configuration in which one container data file is created for each container. |
|                        | multi-container configuration    | configuration in which multiple containers are grouped together into one container data file. |
| date accumulation container | date container configuration   | Data in a container is split on a per-day basis to create a container data file. |


From hereon, container data files for each configuration is described as a **single container data file**, a **multi-container data file**, and **date container data file**, respectively.


In registration update containers, when export is executed by specifying a large container in a  single container data file, file management becomes cumbersome as a large amount of metadata files and row data files are created. Contrastingly, with a multi-container data file, even when a large container is specified, the only files that are output are one meta data file and one row data file.

Therefore, it is recommended that these 2 configurations be **used differently depending on the application**.

A single container data file is used in the following cases.
- To output the current data of a specific container to perform data analysis.
- To create many containers with the same schema as existing containers to register data.

A multi-container data file is used in the following cases.
- To backup a specific container group.
- To move a database to a different GridDB cluster.

In date accumulation containers, there is no such distinction because there is only one type of output file configuration.


### Export execution data file

Data such as the export date and time, the number of containers, container name etc. is saved in the export execution data file. This file is required to directly recover exported data in a GridDB cluster.

[Memo]
- The file name of an export execution data file is gs_export.json.
- Delete the export execution data if an exported container data file is edited manually. A registration error may occur due to discrepancies in the data.
- When importing without any export execution data file, it is essential that the container metadata file be specified. If not, import will fail.


## Configuration of export/import execution environment

The following settings are required to execute an export/import command.

<a id="property_file_settings"></a>
### Property file settings

Set the property file in accordance with the GridDB cluster configuration used by a gsadm user. Property file is `/var/lib/gridstore/expimp/conf/gs_expimp.properties`.

The property file contains the following settings.

| Property | Required | Default value | Note         |
|--------------------------|--------------|--------------------|--------------------|
| mode | Required | MULTICAST | Specify the type of connection method. If the method is not specified, the method used will be the multicast method. <br>MULTICAST ・・MULTICAST: multicast method<br>FIXED_LIST・・fixed list method<br>PROVIDER ・・provider method    |
| hostAddress | Essential if mode=MULTICAST | 239.0.0.1 | Specify the /transaction/notificationAddress in the GridDB cluster definition file (gs_cluster.json). Multicast address used by the export/import tool to access a cluster.    |
| hostPort | Essential if mode=MULTICAST | 31999 | Specify the /transaction/notificationPort in the GridDB cluster definition file (gs_cluster.json). Port of multicast address used by the export/import tool to access a cluster.  |
| jdbcAddress | Essential if mode=MULTICAST | 239.0.0.1 | Specify /sql/notificationAddress in the GridDB cluster definition file (gs_cluster.json) when using the multicast method. |
| jdbcPort | Essential if mode=MULTICAST | 41999 | Specify /sql/notificationPort in the GridDB cluster definition file (gs_cluster.json) when using the multicast method. |
| notificationMember | Essential if mode=FIXED_LIST | － | Specify /cluster/notificationMember/transaction of the cluster definition file (gs_cluster.json) when using the fixed list method to connect. Connect address and port with a ":" in the description. For multiple nodes, link them up using commas. <br>Example)192.168.0.100:10001,192.168.0.101:10001 |
| jdbcNotificationMember | Essential if mode=FIXED_LIST | － | Specify sql/address and sql/port under the /cluster/notificationMember of the cluster definition file (gs_cluster.json) when using the fixed list method to connect. Connect address and port with a ":" in the description. For multiple nodes, link them up using commas. <br>Example)192.168.0.100:20001,192.168.0.101:20001       |
| notificationProvider.url | Essential if mode=PROVIDER | － | Specify /cluster/notificationProvide/url of the cluster definition file (gs_cluster.json) when using the provider method to connect.   |
| restAddress | － | 127.0.0.1 | Specify /system/listenerAddress of the GridDB node definition file (gs_node.json). Parameter for future expansion. |
| restPort | － | 10040 | Specify /system/listenerPort of the GridDB node definition file (gs_node.json). Parameter for future expansion. |
| clusterName | Required | INPUT_YOUR_CLUSTER_NAME_HERE | Specify the cluster name of GridDB which is used in the command "gs_joincluster". |
| logPath | － | /var/lib/gridstore/log | Specify the directory to output the error data and other logs when using the export/import tools. Log is output in gs_expimp-YYYYMMDD.log under the directory.   |
| commitCount | － | 1000 | Specify the number of rows as a unit to register data when registering container data with the import tool. When the numerical value becomes larger, the buffer for data processing gets larger too. If the row size is small, raise the numerical value, and if the row size is large, lower the numerical value. The parameter affects the registration performance for data import. |
| transactionTimeout | － | 2147483647 | Specify the time allowed from the start until the end of a transaction. When registering or acquiring a large volume of data, a large numerical value matching the data volume needs to be set. A maximum value has been specified for processing a large volume of data by default. (Unit: second)   |
| failoverTimeout | － | 10 | Specify the failover time to repeat retry starting from the time a node failure is detected. This is also used in the timeout of the initial connection to the cluster subject to import/export. Increase the value when performing a process such as registering/acquiring a large volume of data in/from a container. (Unit: second)    |
| jdbcLoginTimeout | － | 10 | Specify the time of initial connection timeout for JDBC. (Unit: second)       |
| notificationInterfaceAddress  | － | \*OS-dependent  | To configure the cluster network in multicast mode when multiple network interfaces are available, specify the IP address of the interface to receive the multicast packets from.  |
| intervalTimeZone         | －                           | GMT         | To export to or import from a date accumulation container, set "TimeZone" as the "intervals" option of the export and import commands. As the TimeZone settings, specify either the abbreviated Standard Time name or the time difference from GMT in GMT+HH:mm format.  |

<a id="export_function"></a>
## Export function

The options that can be specified when using the export function is explained here (based on usage examples of the export function).

### Specifying a process target

#### How to specify a container  

There are 3 ways to specify a container from a GridDB cluster, by specifying all the containers of the cluster: by specifying the database, and by specifying the container individually.

**(1) Specify all containers**

- The entire containers and databases in the cluster are applicable.
- Specify --all option.

- [Example]

  ``` example
  $ gs_export --all -u admin/admin
  ```

- [Memo]
  - When executed by a general user, all the containers in the database (in which the general user has access rights to) will be applicable.


**(2) Specify the database**
- All containers in the specified database are applicable.
- Specify the database name with the --db option. Multiple database names can also be specified repeatedly by separating the names with a " " (blank).

- [Example]

  ``` example
  $ gs_export --db db001 db002 -u admin/admin  //Enumerate DB name. Container in the DB Container in the DB
  ```

- [Memo]
  - When executed by a general user, an error will occur if the general user has no access rights to the database specified in --db. (Process can continue if executed by --force.)


**(3) Specify container individually**

- Specified container is applicable
- $ gs_export --container c001 c002 -u admin/admin // Enumerate container name
  - Separate multiple container names with " " (blank) and specify them repeatedly in the --container option.
- Regular expression and specification of the container names
  - Specify the regular expression of the container names with a --containerregex option. A Java regular expression can be used in the specification. Enclose the specification with "" (double quotation) when specifying with a regular expression.

- [Example]

  ``` example
  $ gs_export --container c001 c002 -u admin/admin       //Enumerate container name
  $ gs_export --containerregex "^c0" -u admin/admin      //regular expression specification: Specify containers whose container name start with c0
  ```

- [Memo]
  - Specify the name of the applicable database with the --prefixdb option in the --container/--containerregex option.
    If the --prefixdb option is omitted, the container in the default connection destination database "public" will be processed.
  - When executed by a general user, an error will occur if the general user has no access rights to the database where the container specified in the --container/--containerregex option is stored. (Process can continue if executed by --force.)


#### How to specify a row  

In exporting a container, it is possible to specify  the condition for determining which rows to output.
To specify this condition, the following methods are provided for each export container type:

|  Export import type             | Specification type                  | Method    |
| ------------------------- | ---------------------------- | ------- |
| registration update container         | specification using a search query      | Specify by defining a container name and a search query.      |
| date accumulation container | specification using a time interval | Specify the date range during which a row (TIMESTAMP type) is extracted. |

For a container for which no condition is specified, all rows stored in it will be exported.

◆Specification using a search query

 This is a method that can be used when the export container type is a registration update container.
 
- Using the --filterfile option, specify a definition file that describes a container name and a search query. In the definition file, describe the name of a container to which a search query is applied,  and the search query.

[Example of running the command]

``` example
$ gs_export -c c001 c002 -u admin/admin --filterfile filter1.txt

$ gs_export --all -u admin/admin --filterfile filter2.txt
```

[Example] Description of definition file

``` example
^cont_month     :select * where time > 100
^cont_minutes_.*:select * where flag = 0
cont_year2014   :select * where timestamp > TIMESTAMP('2014-05-21T08:00:00.000Z')
```

[Memo]
- Specify the containers with a regular Java expression.  Example: If "container1" is used in the description, all containers containing container1 will be relevant (container10, container12 etc.). If fully consistent, use "^container1$" in the description.
- Among the containers subject to export which are specified in the --all and -c options, all rows in containers which the definition described in the definition file does not apply to will be exported.
- To describe the container and search query in 1 line, use a ":" for the separation.
- If the container applies to multiple definitions, the definition described at the beginning will be applied.
- Describe the file in the UTF-8 format.
- Execute the export test function to check whether the description of the definition file is correct.


◆Specification using a time interval

 This is a method that can be used when the export container type is a date accumulation container.

- Using the --intervals option, specify the date range for which rows are retrieved. The system would then compare this data range with the container key (for a date accumulation container, TIMESTAMP type).

[Example of running the command]

``` example
$ gs_export -c tsc001 tsc002 -d ./2021/ --intervals 20210101:20211231

$ gs_export --all -u admin/admin -d ./20210131/ --intervals 20210101:20210131

```

[Memo]
- When a time interval is not specified, data in the target containers is output for all periods.
- The --all option specifies all containers, of which only date accumulation containers are used for output.
- As multiple row data files are output, it is recommended to specify the destination directory using the -d option together with the --all option.

#### How to specify user access rights

Information on GridDB cluster users and their access rights can also be
exported. Use the following command when migrating all data in the
cluster.

- The [--acl] option must be used with the [--all] option or the [--db] option . However, only user information of a general user can be exported. Migrate the data on the administrator user separately (copy the user definition file).

1. Specify the `--all` option and `--acl` option.

    \[Example\]

    ```example
    $ gs_export --all -u admin/admin --acl
    ```

2. Specify the `--db` option and `--acl` option.

    \[Example\]

    ```
    $ gs_export --db testdb1 testdb2 -u admin/admin --acl
    ```

\[Memo\]

- The command needs to be executed by an administrator user.

- For v4.5 metadata file `gs_export_acl.json` add and change some out parameters:

  | Tag Name                     | Value                  | Note                                                                |
  | ---------------------------- | ---------------------- | ------------------------------------------------------------------- |
  | /user[n]/isRole              | true or false          | whether it is a role. <br />true: is a role <br />false: not a role |
  | /user[n]/username            | user name or role name | Indicates the name of the user or the name of the role.             |
  | /database[n]/acl[n]/username | user name or role name | Indicates the name of the user or the name of the role.             |

  Example of `gs_export_acl.json` v4.5

  ```
  {
      "version":"4.5.0",
      "user":
      [
          {
              "isRole":false,
              "username":"user1",
              "password":"..."
          },
          {
              "isRole":true,
              "username":"role01",
          }
      ],
      "database":
      [
          {
              "name":"db1",
              "acl":
              [
                  {
                      "username":"user1",
                      "role":"READ"
                  },
                  {
                      "username":"role01",
                      "role":"READ"
                  }
              ]
          }
      ]
  }

  ```

#### How to specify a view

A view of a GridDB cluster can also be exported as well as the container.

  Specify --all option or --db option to export the view of the database to be exported.

``` example
$ gs_export --db public -u admin/admin
Export Start.
Directory : /tmp/export
     :
Number of target container:5 ( Success:5 Failure:0 )

The number of target  views : 15
Export Completed.
```


### Specifying the output format of a row data file

A CSV data file or binary data file can be specified as the output format of a row data file.
The -d option can be used for both a registration update container and a date accumulation container.

- Output in the CSV data file  
  - Execute an export command without specifying the --binary option

- Output in the binary data file  
  - Specify the --binary \[file size upper limit\] option.
  - Split the binary data file using the specified file size upper limit and export the file.
  - The file size upper limit is specified in Mbytes. If the file size upper limit is not specified, the size upper limit will be 100 Mbytes. The maximum file size that can be specified is 1,000 Mbytes.

[Example]

``` example
$ gs_export -c c001 c002 -u admin/admin --binary
    
$ gs_export --all -u admin/admin --binary 500       //Export Completed.
```

### Specifying the output configuration of container data file

In exporting a registration update container, it is possible to specify the configuration of a container data file to be output.

A single container data file to create container data file in a container unit, or a multi-container data file to output all containers to a single container data file can be specified.

- Output using a single container data file  
  - If the --out option is not specified during export, the data will be output using a single container data file.

- Output using a multi-container data file  
  - It specifies the --out \[file identifier\] option. By specifying the file identifier, the file name of the metadata file will become "file identifier_properties.json".
    The file will be named "file identifier.csv" or "file identifier_divN.mc" (N is the sequential number of the divided files) if the multi-container data file format is CSV, or binary respectively.
    The number of characters in the file identifier is limited to 20.
  - If the file identifier is omitted in the --out \[file identifier\] option, the file identifier is the representation of executed date. (Example: 20131031_155015_810_properties.json, 20131031_155015_810.csv)

[Example]

``` example
$ gs_export -c c001 c002 -u admin/admin --out test
    
$ gs_export --all -u admin/admin --out           //file is created with the date
```

### Specifying the output destination

The directory of the container data file can be specified as the output destination. Create a directory if the specified directory does not exist.
If the directory is not specified, data will be output to the current directory when a command is executed. Use the -d option to specify the output destination.

[Example]

``` example
$ gs_export --all -u admin/admin --out test -d /tmp
```

[Memo]
-   A directory which already contains container data files cannot be specified.

### Specifying the number parallel executions

Get data to access a cluster in parallel with the export tool. If a command is executed in parallel on a cluster composed of multiple nodes, data can be acquired at a high speed as each node is accessed in parallel.
-   Execute in parallel for the specified number by specifying the --parallel option. When executed in parallel, the export data will be divided by the same number as the number of parallel executions. A range from 2 to 32 can be specified.

[Memo]
-   The --parallel option can be specified only if the binary format (--binary option) and multi-container format (--out option) are specified.

[Example]

``` example
$ gs_export --all -u admin/admin --binary --out --parallel 4
```

### Test execution function

Before exporting a container, the user can assess whether the export can be carried out correctly.

- Specify test execution  
  - The export sequence can be checked simply by adding the --test option to the export command. No files will be created as the process simply checks but does not actually acquire any data.
  - The information on the container name, partition ID, and the number of rows is displayed during the test.

[Example]

``` example
$ gs_export -u admin/admin --all --test
Export Start.
[TEST Mode]
Directory : /var/lib/gridstore/export
The number of target containers  : 5

Name                                      PartitionId Row
------------------------------------------------------------------
public.container_2                                 15          10
public.container_3                                 25          20
public.container_0                                 35          10
public.container_1                                 53          10
public.container_4                                 58          20

Number of target container:5 ( Success:5 Failure:0 )

The number of target views : 15
Export Completed.
```

### Error continuation specification

Export processing can be continued even if a row data acquisition error were to occur due to a lock conflict with another application.

-   By specifying the --force option, the export process will continue from the row data of the next container even if an acquisition error were to occur in a row data.

[Example]

``` example
$ gs_export --all -u admin/admin --force
```

[Memo]
-   Regarding containers which skipped the processing due to an error, data will still be output to the container data file even though it is not complete. However, import processing will not be carried out as the data will not be recorded in the export execution file. After resolving the row data acquisition error, execute the export process for the relevant container again.

### Other functions

**Detailed settings in the operating display**
-   Processing details can be displayed by specifying the --verbose option.

[Example]

``` example
$ gs_export --containerregex "^c0" -u admin/admin --verbose
Export Start.
Directory : /data/exp
Number of target container : 4

public.c003 : 1
public.c002 : 1
public.c001 : 1
public.c010 : 1
The row data has been acquired. : time=[5080]

Number of target container:4 ( Success:4 Failure:0 )
Export Completed.
```

**Suppressed settings in the operating display**  
-   The processing status display can be suppressed by specifying the --silent option.

[Example]

``` example
$ gs_export -c c002 c001 -u admin/admin --silent
```

## Import function

Import the container data file data into the GridDB cluster.

### Types of data source for import

The input data sources used by the import tool are as follows.

- Container data file: Container data saved by the export function, or container data created by the user

### Importing from a container data file

Use the export function to import data in the exported data format into a GridDB cluster.

#### Specifying a process target

Processing data to be imported from the container data file needs to be specified.

##### How to specify a container

There are 3 ways to specify a container, by specifying all the containers in the container data file, by specifying the database, and by specifying the container individually.

**(1) Specify all containers**

- All containers in all the databases are applicable
- Specify --all option.

- [Example]

  ``` example
  $ gs_import --all -u admin/admin
  ```

**(2) Specify the database**  

- All containers in the specified database are applicable.
- Enumerate a database name.
  - Specify multiple database names repeatedly by separating the names with a " " (blank) in the --db option.

- [Example]

  ``` example
  $ gs_import --db db001 db002 -u admin/admin  //Enumerate DB name. Container in the DB Container in the DB
  ```

**(3) Specify container individually**

- Specified container is applicable.
- $ gs_export --container c001 c002 -u admin/admin // Enumerate container name
  - Separate multiple container names with " " (blank) and specify them repeatedly in the --container option.
- Regular expression and specification of the container names
  - Specify the regular expression of the container names with a --containerregex option. A Java regular expression can be used in the specification. Enclose the specification with "" (double quotation) when specifying with a regular expression.

- [Example]

  ``` example
  $ gs_import --container c001 c002 -u admin/admin  //Enumerate container name
  $ gs_import --containerregex "^c0" -u admin/admin      //regular expression specification: Specify containers whose container name start with c0
  ```

[Points to note]
- If the exported data in GridDB V3.2 or earlier includes the NewSQL tables created using NewSQL I/F, the NewSQL tables will be imported as collections (tables) in GridDB V3.5 or later. In addition, the index names attached to the indices of the NewSQL tables are not imported.

[Memo]
- When executed by an administrator user, a database will be created if the database does not exist at the storage location of the container.
- When executed by a general user, an error will occur if the general user has no access rights, or if the database does not exist at the storage location of the container. (Process can continue if executed by --force.)
- Specify the name of the applicable database with the --prefixdb option if the --container/--containerregex option is specified.
  If the --prefixdb option is omitted, the default connection destination database "public" will be processed.
- Check the container list stored in the container data file with the --list option.


##### How to specify user access rights

If data is exported by specifying the --acl option in the export function, data on the user and access rights can also be imported.  Use the following command when migrating all data in the cluster.

1. Specify the `--all` option and `--acl` option.

    \[Example\]

    ```example
    $ gs_import --all --acl -u admin/admin
    ```

2. Specify the `--db` option and `--acl` option.

    \[Example\]

    ```
    $ gs_import --db testdb1 testdb2 --acl -u admin/admin
    ```

\[Memo\]

- The command needs to be executed by an administrator user.
- Use the following command when migrating all data in the cluster.
  Execute the command without any databases and general users existing
  in the migration destination.


##### How to specify a view

If the view was exported using the export function, a view can also be imported together with the container data.

Specify --all option or --db option to import the view of the database to be imported.


[Memo]
- When --replace option is specified, the view is imported as follows:
  - An error will occur if a container with the same name exists, even if --force option is specified.
  - When the view with the same name exists, the existing view is deleted and a new one is create.
- If some of the created views cannot be referred to, a message will appear.


#### Specifying a container data file

Specify a container data file to import.
The date range for which import is executed can be specified only for a date accumulation container.

| How to specify                 |   Export container type               | Description |
| ------------------------ | -------------------------- | ------- |
| Specify a directory.        | registration update container       |  Specify a container to import within the directory. |
|        -                  | data accumulation container |  same as above |
| Specify a time interval.        | date accumulation container only       |  Specify the date range for which import is executed. |

◆Specifying a directory
  - With the --d option, specify a directory where container data files are located.
  - If the directory is not specified, container data files in the current directory during command execution are imported.

[Example]

``` example
//Specify all containers from the current directory
$ gs_import --all -u admin/admin

//Specify multiple databases from a specific directory
$ gs_import --db db002 db001 -u admin/admin  -d /data/expdata

//Specify multiple containers from a specific directory
$ gs_import -c c002 c001 -u admin/admin  -d /data/expdata
```

[Memo]
- If the export execution data file (gs_export.json) does not exist, e.g. because the container data file is created manually; specify the metadata file (XXXXX_properties.json) using the -f option. If the -f command is not specified, import will fail.

◆Specifying a time interval
 - With the --intervals option, specify the date range for which import is executed.
 - If the date range is not specified, container data files will be imported for all periods.

[Example]

``` example
//Specify all containers in the current directory and import only the data in a specific interval that has been specified.
$ gs_import --all -u admin/admin --intervals 20210101:20211231

//Specify multiple databases in a specific directory and import only the data in a specific interval that has been specified.
$ gs_import --db db002 db001 -u admin/admin -d ./2021/ --intervals 20210101:20211231

//Specify multiple containers in a specific directory and import only the data in a specific interval that has been specified.
$ gs_import -c c002 c001 -u admin/admin  -d ./20210131/ --intervals 20210101:20210131
```

[Memo]
- This "intervals " option is applicable only to export data in a date accumulation container. It cannot be specified for a registration update container.


#### Getting a container list

The container data can be checked before importing.

[Example]

``` example
$ gs_import --list
Container List in local export file
DB            Name              Type            FileName
public        container_2       COLLECTION      container_2.csv
public        container_0       TIME_SERIES     container_0.csv
public        container_1       COLLECTION      container_1.csv
userDB        container_1_db    TIME_SERIES     userDB.container_1_db.csv
userDB        container_2_db    TIME_SERIES     userDB.container_2_db.csv
userDB        container_0_db    COLLECTION      userDB.container_0_db.csv
```

### Data registration option

When importing, if a specific option is not specified, an error will occur if the container that you are trying to register already exists in the GridDB cluster.
Data can be added or replaced by specifying the next option. During data registration, the number of containers registered successfully and the number of containers which failed to be registered are shown.

- Add/update data  
  - Data can be registered and updated in an existing container by specifying the --append option
  - Data can be added, registered or updated only if the  existing container and the specified container have the same container definition data, including the container schema and index setting data.
  - The registration procedure according to the type of container is as follows.

    | Container type | Row key assigned | Behavior                                                                     |
    |----------------|------------|--------------------------------------------------------------------------|
    | Collection | TRUE | Columns with the same key will be updated while data with different keys will be added.           |
    |                | FALSE | All row data will be added and registered.                                         |
    | Timeseries | TRUE | The data will be added and registered if it has more recent time than the existing registration data. <br>If the data has the same time as the existing data, the column data will be updated. |

  - For timeseries containers, if the time of the data to be registered is the same as the existing data, the existing row is overwritten. The number of rows is not increased.
  - By specifying the --schemaCheckSkip option, the existing container definition data is not checked.
    Specify this option, when you import data different from the existing container definition, such as importing a new index.

- Replace container  
  - Delete the existing container, create a new container, and register data in it by specifying the --replace option.

[Example]

``` example
$ gs_import -c c002 c001 -u admin/admin  --append
Import initiated (Append Mode)
Import completed
Success:2 Failure:0
  
$ gs_import -c c002 c001 -u admin/admin  --replace
Import initiated (Replace Mode)
Import completed
Success:2 Failure:0
  
$ gs_import --all  -u admin/admin  -d /datat/expdata   --replace
```


### Error continuation specification

The import process can be continued even if a registration error were to occur in a specific row data due to a user editing error in the container data file.
-   By specifying the --force option, the import process will continue from the row data of the next container even if a registration error were to occur in the row data.

[Example]

``` example
$ gs_import --all -u admin/admin -d /data/expdata --force
```

[Memo]
-   Specify the container replacement option (--replace) to re-register a collection in which an error has occurred after revising the container data file.

### Other functions

**Detailed settings in the operating display**  
-   Processing details can be displayed by specifying the --verbose option.

**Progress output log when importing container**

- Executed row numbers can be displayed in the log file by specifying the `--process <interval row number>` option.
- Interval row number must be positive integer number.

\[Example\]

```
$ gs_import --all -u admin/admin --process 10
Import Start.
Number of target containers : 100

test.dummy : 1

Number of target containers:1 ( Success:1  Failure:0 )
Import Completed.
```

Log output in `gs_expimp-YYYYMMDD.log`:

```
2023-04-26T17:49:29.413Z INFO [main] com.toshiba.mwcloud.gs.tools.expimp.importMain [importMain.java::45] Import Start. :Version 5.3.00
2023-04-26T17:49:29.482Z INFO [main] com.toshiba.mwcloud.gs.tools.expimp.propertiesInfo [propertiesInfo.java::361] Property : notificationMember=[10.0.0.124:10001] jdbcNotificationMember=[10.0.0.124:20001] clusterName=[MyCluster] commitCount=[1000] transactionTimeout=[2147483647] failoverTimeout=[10] jdbcLoginTimeout=[10]
2023-04-26T17:49:29.483Z INFO [main] com.toshiba.mwcloud.gs.tools.expimp.cmdAnalyze [cmdAnalyze.java::737] Parameter : --all --user=[admin] --directory=[csvtest2] --progress
2023-04-26T17:49:29.565Z INFO [main] com.toshiba.mwcloud.gs.tools.expimp.importProcess [importProcess.java::208] Get Container Information from metaInfoFile. : containerCount=[1] time=[72]
2023-04-26T17:49:30.209Z INFO [main] com.toshiba.mwcloud.gs.tools.expimp.importProcess [importProcess.java::1151] dummy: 10 rows imported.
2023-04-26T17:49:30.222Z INFO [main] com.toshiba.mwcloud.gs.tools.expimp.importProcess [importProcess.java::1151] dummy: 20 rows imported.
2023-04-26T17:49:30.234Z INFO [main] com.toshiba.mwcloud.gs.tools.expimp.importProcess [importProcess.java::1151] dummy: 30 rows imported.
2023-04-26T17:49:30.246Z INFO [main] com.toshiba.mwcloud.gs.tools.expimp.importProcess [importProcess.java::1151] dummy: 40 rows imported.
2023-04-26T17:49:30.260Z INFO [main] com.toshiba.mwcloud.gs.tools.expimp.importProcess [importProcess.java::1151] dummy: 50 rows imported.
2023-04-26T17:49:30.275Z INFO [main] com.toshiba.mwcloud.gs.tools.expimp.importProcess [importProcess.java::1151] dummy: 60 rows imported.
2023-04-26T17:49:30.290Z INFO [main] com.toshiba.mwcloud.gs.tools.expimp.importProcess [importProcess.java::1151] dummy: 70 rows imported.
2023-04-26T17:49:30.303Z INFO [main] com.toshiba.mwcloud.gs.tools.expimp.importProcess [importProcess.java::1151] dummy: 80 rows imported.
2023-04-26T17:49:30.318Z INFO [main] com.toshiba.mwcloud.gs.tools.expimp.importProcess [importProcess.java::1151] dummy: 90 rows imported.
2023-04-26T17:49:30.327Z INFO [main] com.toshiba.mwcloud.gs.tools.expimp.importProcess [importProcess.java::1151] dummy: 100 rows imported.
2023-04-26T17:49:30.358Z INFO [main] com.toshiba.mwcloud.gs.tools.expimp.importProcess [importProcess.java::983] import: db,test,name,dummy,rowCount,100,Time all,508,put,30,readRow,26,readMeta,4,createDrop,30,search,29,other,389
2023-04-26T17:49:30.363Z INFO [main] com.toshiba.mwcloud.gs.tools.expimp.commandProgressStatus [commandProgressStatus.java::112] Number of target containers:1 ( Success:1  Failure:0 )
2023-04-26T17:49:30.364Z INFO [main] com.toshiba.mwcloud.gs.tools.expimp.importMain [importMain.java::91] Import Completed.: time=[962]
```

## Command/option specifications

### Export command

- Command list

  | Command | Option/argument |
  |----------|----------------------------------------------------------------|
  | gs_export | -u｜--user \<User name\>/\<Password\><br>--all ｜ --db \<database name\> \[\<database name\>\] \| ( --container \<container name\> \[\<container name\>\] ... \| --containerregex \<regular expression\> \[\<regular expression\>\] ...)<br>\[-d｜--directory \<output destination directory path\>\] <br>\[--out \[\<file identifier\>\] <br>\[--binary \[\<file size\>\]\] <br>\[--filterfile definition file name\] <br>\[--parallel \<no. of parallel executions\>\] <br>\[--acl\] <br>\[--prefixdb \<database name\>\] <br>\[--force\] <br>\[-t｜--test\] <br>\[-v｜--verbose\] <br>\[--silent\] <br>\[--schemaOnly\]    |
  | gs_export | --version                                |
  | gs_export | \[-h｜--help\]                           |

- Options

  | Options       | Required | Note      |
  |----------------------------------------|------|-----------|
  | -u\|--user \<user name\>/\<password\> | ✓ | Specify the user and password used for authentication purposes.       |
  | --all | ✓ | All containers of the cluster shall be exported. Either --all, --container, --containerregex, --db option needs to be specified.  |
  | --db | ✓ | All containers in the specified database shall be exported. Either --all, --container, --containerregex, --db option needs to be specified.       |
  | -c\|--container \<container name\> ... | ✓ | Specify the container to be exported. Multiple specifications are allowed by separating them with blanks. Either --all, --container, --containerregex, --db option needs to be specified.        |
  | --containerregex \<regular expression\> ... | ✓ | Specify the containers by the regular expression to be exported. Multiple specifications are allowed by separating them with blanks. When using a regular expression, enclose it within double quotations to specify it. Either --all, --container, --containerregex, --db option needs to be specified. This option can be used in combination with --container option.      |
  | -d\|--directory \<output destination directory path\> | | Specify the directory path of the export destination. Default is the current directory.        |
  | --out \[\<file identifier\>\] | | Specify this when using the multi-container format for the file format of the output data. The single container format will be used by default. The number of characters in the file identifier is limited to 20. <br>If the file identifier is specified, the file identifier will be used as the file name, and if it is omitted, the output start date and time will be used as the file name.    |
  | --binary \[\<file size\>\] | | Specify this when using the binary format for the output format of the row data file. The CSV format will be used by default. <br>Specify the output file size in MB. Default is 100MB. A range from 1 to 1000 (1GB) can be specified.      |
  | --filterfile \<definition file name\> | | Specify the definition file in which the search query used to export rows is described. All rows are exported by default.   |
  | --intervals YYYYMMdd:YYYYMMdd          |      | If a container to be exported from is a date accumulation container, specify the date range for which rows are retrieved for export in "YYYYMMdd:YYYYMMdd" format, consisting of a start date and an end date separated by a colon. If the date range is not specified, all rows will be exported.  The "intervals" option cannot be combined with the "filterfile" option.   |
  | --parallel \<no. of parallel executions\> | | Execute in parallel for the specified number. When executed in parallel, the export data will be divided by the same number as the number of parallel executions. This can be specified only for the multi-container format (when the --out option is specified). A range from 2 to 32 can be specified.      |
  | --acl | | Data on the database, user, access rights will also be exported. This can be specified only if the user is an administrator user and the --all option or the --db option is specified. |
  | --prefixdb \<database name\> | | If a --container option is specified, specify the database name of the container. The containers in the default database will be processed if they are omitted. |
  | --force | | Processing is forced to continue even if an error occurs. Error descriptions are displayed in a list after processing ends.          |
  | -t\|--test | | Execute the tool in the test mode.                  |
  | -v\|--verbose | | Output the operating display details.     |
  | --silent | | Operating display is not output.   |
  | --schemaOnly                |   | Export container definitions only; row data is not exported.   |
  | --version | | Display the version of the tool.      |
  | \-h\|--help    |          | Display the command list as a help message.         |

[Memo]
-   If the -t (--test) option is specified, a query will be executed until the data is fetched. Container data file will not be created.
-   If the -v (--verbose) option is specified, a message will appear during processing. If omitted, a message will appear only when there is an error.
-   Create the respective directory and file if the specified directory path does not exist in the -d (--directory) option, and the specified file name does not exist in the --out option.
-   If a --containerregex option is specified, Java regular expressions can be specified for the container names. See the Java "Class Pattern" for details.
-   In specifying the --intervals option, if a start date and/or an end date are unknown, specify an extremely low value (19700101, for example) and/or an extremely high value (99991231, for example), respectively.

### Import command

- Command list

  | Command | Option/argument |
  |---------------------|--------------------------------------|
  | gs_import | -u｜--user \<User name\>/\<Password\><br>--all ｜ --db \<database name\> \[\<database name\>\] \| ( --container \<container name\> \[\<container name\>\] ... \| --containerregex \<regular expression\> \[\<regular expression\>\] ...)<br>--db \<database name\> \[\<database name\>\]<br>\[--append｜--replace\]<br>\[-d｜--directory \<import target directory path\>\]<br>\[-f｜--file \<file name\> \[\<file name\> ...\]\]<br>\[--count \<commit count\>\]<br>\[--acl\]<br>\[--prefixdb \<database name\>\]<br>\[--force\]<br>\[--schemaCheckSkip\]<br>\[-v｜--verbose\]<br>\[--silent\]<br>\[--progress \<interval number of rows\>\]     |
  | gs_import | -l｜--list<br>\[-d｜--directory \<directory path\>\]<br>\[-f｜--file \<file name\> \[\<file name\> ...\]\]      |
  | gs_import | --version                                   |
  | gs_import | \[-h｜--help\]                                         |

- Options

  | Options       | Required | Note                                  |
  |------------------------------------|------|--------------------------------------|
  | \-u\|--user \<user name\>/\<password\>            | ✓        | Specify the user and password used for authentication purposes.                          |
  | \--all                                           | ✓        | All containers in the import source file shall be imported. Either --all, --container, --containerregex, --db option needs to be specified.                                                                                    |
  | \--db                                            | ✓        | All containers in the specified database shall be imported. Either --all, --container, --containerregex, --db option needs to be specified.                                                                                                                     |
  | \-c\|--container \<container name\> ...           | ✓        | Specify the container subject to import. Multiple specifications are allowed by separating them with blanks. Either --all, --container, --containerregex, --db option needs to be specified.                                                                                                            |
  | \--containerregex \<regular expression\> ...     | ✓        | Specify the containers by regular expressions subject to import. Multiple specifications are allowed by separating them with blanks. When using a regular expression, enclose it within double quotations to specify it. Either --all, --container, --containerregex, --db option needs to be specified. This option can be used in combination with --container option. |
  | \--append                                        |          | Register and update data in an existing container.  |
  | \--replace                                       |          | Delete the existing container, create a new container, and register data.          |
  | \-d\|--directory \<import target directory path\> |          | Specify the directory path of the import source. Default is the current directory.|
  | \-f\|--file \<file name\> [\<file name\> ...]   |          | Specify the container data file to be imported. Multiple specifications allowed. All container data files of the current directory or directory specified in d (--directory) will be applicable by default.   |
  | --intervals YYYYMMdd:YYYYMMdd          |      | If a container to be imported from is a date accumulation container, specify the date range for which rows data files are retrieved for import in "YYYYMMdd:YYYYMMdd" format, consisting of a start date and an end date separated by a colon. If the date range is not specified, all row data files will be imported.   |
  | \--count \<commit count\>                        |          | Specify the number of input cases until the input data is committed together. |
  | \--acl                                           |          | Data on the database, user, access rights will also be imported. This can be specified only if the user is an administrator user and the --all option or the --db option is specified for data exported by specifying the --acl option.   |
  | \--prefixdb \<database name\>                    |          | If a --container option is specified, specify the database name of the container. The containers in the default database will be processed if they are omitted.        |
  | \--force                                         |          | Processing is forced to continue even if an error occurs. Error descriptions are displayed in a list after processing ends.   |
  | \--schemaCheckSkip                               |          | When --append option is specified, a schema check of the existing container will not be executed.   |
  | \-v\|--verbose                                    |          | Output the operating display details.      |
  | \--progress \<interval number of rows\>          |          | Write in log file after an interval number of rows were imported  |
  | \--silent                                        |          | Operating display is not output.  |
  | \-l\|--list                                       |          | Display a list of the specified containers to be imported.  |
  | \--version                                       |          | Display the version of the tool.   |
  | \-h\|--help    |          | Display the command list as a help message.  |

[Memo]
-   If -l (--list) option is specified, and options other than the -d (--directory) and -f (--file) option are specified, an option argument error will occur.
-   If the -v (--verbose) option is specified, a message will appear during processing. If omitted, a message will appear only when there is an error.
-   If --containerregex option is specified, Java regular expressions can be used to specify the container names. See the Java "Class Pattern" for details.
-   In specifying the --intervals option, if a start date and/or an end date are unknown, specify an extremely low value (19700101, for example) and/or an extremely high value (99991231, for example), respectively.

<a id="format_of_container_data_file"></a>
## Format of container data file

The respective file formats to configure container data files are shown below.

<a id="metadata_file"></a>
### Metadata file

The metadata file stores the container data in the JSON format.  The container data to be stored is shown below.

| Item | Note                                                                                      |
|--------------------------------|-----------------------------------------------------------------------------------------|
| \<Container name\>                | Name of the container.                                               |
| Container type                    | Refers to a collection or time series container.                         |
| Schema data                       | Data of a group of columns constituting a row. Specify the column name, data type, and column constraints.         |
| Index setting data                | Index type data set in a container. Availability of index settings. Specify the type of index, including tree index and spatial index.   |
| Row key setting data              | Set up a row key when collection container is used. For time series containers, either there is no row key set or the default value, if set, will be valid. |
| Table partitioning data           | Specify table partitioning data.                      |
| Time interval information         | For a date registration container, specify information about a row data file that is split on a per-day basis.  |

The tag and data items of the metadata in the JSON format are shown below. Tags that are essential for new creations by the user are also listed (tag setting condition).

| field | Item | Note | Setting conditions        |
|-------------------------|-----------------------------|---------|------------------|
| Common parameters           |                             |                                |                   |
| database | \<Database name\> | \<Database name\> | Arbitrary, "public" by default        |
| container | \<Container name\> | \<Container name\> | Required                                |
| containerType | Container type | Specify either COLLECTION or TIME_SERIES | Required       |    
| containerFileType | Container data file type | Specify either csv or binary. | Required             |
| containerFile | Container data file name | File name | Arbitrary             |
| dataAffinity | Data affinity name | Specify the data affinity name. | Arbitrary             |
| partitionNo | Partition | Null string indicates no specification. | Arbitrary, output during export. Not used even if it is specified when importing.    |
| columnSet | Column data set (, schema data) | Column data needs to match when adding data to an existing container | Required    |
| &emsp;columnName | Column name | | Required              |
| &emsp;type | JSON Data type | Specify either of the following values: BOOLEAN/ STRING/ BYTE/ SHORT/ INTEGER/ LONG/ FLOAT/ DOUBLE/ TIMESTAMP/ GEOMETRY/ BLOB/ BOOLEAN\[\]/ STRING\[\]/ BYTE\[\] /SHORT. \[\]/ INTEGER\[\]/ LONG\[\]/ FLOAT\[\]/ DOUBLE\[\]/ TIMESTAMP\[\]. | Required    |
| &emsp;notNull | NOT NULL constraint | true/false | Arbitrary, "false" by default   |
| rowKeyAssigned | Row key setting (\*1) | specify either true/false<br>Specifying also rowKeySet causes an error | Arbitrary, "false" by default     |
| rowKeySet | Row key column names | Specify row key column names in array format.<br>The row key needs to match when adding data to an existing container | Arbitrary (\*2)    |
| indexSet | Index data set | Can be set for each column.  Non-existent column name will be ignored or an error will be output. | Arbitrary      |
| &emsp;columnNames | Column names | Specify column names in array format. | Arbitrary (essential when indexSet is specified)     |
| &emsp;type | Index type | Specify one of the following values: TREE (STRING/ BOOLEAN/ BYTE/ SHORT/ INTEGER/ LONG/ FLOAT/ DOUBLE/ TIMESTAMP) or SPATIAL (GEOMETRY). | Arbitrary (essential when indexSet is specified)      |
| &emsp;indexName | Index name | Index name | Arbitrary, not specified either by default or when null is specified.     |
| Table partitioning data  |                 |     |      |
| tablePartitionInfo | Table partitioning data | For Interval-Hash partitioning, specify the following group of items for both Interval and Hash as an array in that order | Arbitrary     |
| type | Table partitioning type | Specify either HASH or INTERVAL | Essential if tablePartitionInfo is specified |
| column | Partitioning key | Column types that can be specified are as follows<br>Any type if type=HASH<br>BYTE, SHORT, INTEGER, LONG, TIMESTAMP if type=INTERVAL | Essential if tablePartitionInfo is specified     |
| divisionCount | Number of hash partitions | (Effective only if type=HASH) Specify the number of hash partitions | Essential if type=HASH     |
| intervalValue | Interval value | (Effective only if type=INTERVAL) Specify the interval value | Essential if type=INTERVAL |
| intervalUnit | Interval unit | (Effective only if type=INTERVAL) DAY only | Essential if type=INTERVAL and column=TIMESTAMP  |
| Interval or interval-hash partitioning only parameter |         |     |
| expirationType | Type of expiry release function | Specify "partition", when specifying partition expiry release. | Arbitrary   |
| expirationTime | Length of expiration | Integer value | Essential if expirationType is specified  |
| expirationTimeUnit | Elapsed time unit of row expiration | Specify either of the following values: DAY/ HOUR/ MINUTE/ SECOND/ MILLISECOND. | Essential if expirationType is specified    |
| Time interval information |         |     |
| timeIntervalInfo     | Time interval information         | For a date accumulation container, describe the following information in array format. | Arbitrary     |
| containerFile        | Container data file name     | File name  | Required if timeIntervalInfo is specified |
| boundaryValue        | Date range                | date to start container data      | Required if timeIntervalInfo is specified |
| intervalWorkerGroup                                   | Interval worker group               | Specify what worker group is the container on                                                                                                                                                                             | Essential if containerType is TIME_SERIES or COLLECTION and tablePartitionInfo's type is INTERVAL and the partitioning key is TIMESTAMP |
| intervalWorkerGroupPosition                           | Position of interval worker group   | Specify the position of the worker group                                                                                                                                                                                  | Essential if containerType is TIME_SERIES or COLLECTION and tablePartitionInfo's type is INTERVAL and the partitioning key is TIMESTAMP |


- \* 1: Information output to metadata file before V4.2. Use rowKeySet in V4.3 or later.
- \* 2: Required when containerType is TIME_SERIES and rowKeyAssigned is false.


[Memo]
- Metadata file is written in UTF-8.
- Container metadata is described in the json format in the metadata file of a single container data file.
- Container metadata is described in a json **array** in the metadata file of a multi-container data file.

- When exporting, the metadata file is named according to the following rules.
  - When the format is a single container format
    - \<database name\>.\<container name\>_properties.json
    - The database and container name in the file name are URL-encoded. If the length of "encoded database name.encoded container name" is over 140 characters, the file name is modified as connecting 140 characters from the beginning and the sequential number.

    - Example:

      ``` example
      In the case of importing the next three containers,
      * database "db1", container "container_ ... _2017/08/01" (the container name that contains over 140 characters)
      * database "db1", container "container_ ... _2017/09/01" (the container name that contains over 140 characters)
      * database "db1", container "container_ ... _2017/10/01" (the container name that contains over 140 characters)

      the name of each metadata file will be the container name encoded, trimmed to be less than 140 characters, and consecutive numbers added, like as follows:
      db1.container・・・2017%2f08_0_properties.json
      db1.container・・・2017%2f09_1_properties.json
      db2.container・・・2017%2f10_2_properties.json
      ```

  - When the format is a multi-container format
    - A file identifier is specified by --out option: \<file identifier\>_properties.json
    - The file identifier is omitted in --out option: YYYYMMDD_HHmmss_SSS_properties.json

[Notes]
-   Do not edit metadata files if row data files are exported in the binary format.


[Example1] Example of a collection in a single container data file (public.c001_properties.json)
- A single collection is described.

  ``` json
  {
      "container": "c001",
      "containerFile": "public.c001.csv",
      "containerFileType": "csv",
      "containerType": "COLLECTION",
      "columnSet": [
          { "columnName": "COLUMN_ID",  "type": "INTEGER" },
          { "columnName": "COLUMN_STRING", "type": "STRING"}
      ],
      "indexSet": [
          { "columnName": "COLUMN_ID", "type": "TREE"},
          { "columnName": "COLUMN_STRING", "type": "TREE" }
      ],
      "rowKeyAssigned": true
  }
  ```

[Example 2] Example of a collection and timeseries container in a multi-container data file (public.container01_properties.json)
- For collections and timeseries containers >

  ``` json
  [
      {
          "container": "c001",
          "containerType": "collection",
          "containerFileType":"csv",
          "containerFile":"public.container01.csv",
          "rowKeyAssigned":true,
          "columnSet": [
              { "columnName": "COLUMN_FLAG", "type": "BOOLEAN" },
              { "columnName": "COLUMN_BLOB_DATA", "type": "BLOB" },
              { "columnName": "COLUMN_STRING", "type": "STRING" }
          ],
          "indexSet":[
              { "columnName":" COLUMN_STRING ", "indexType": "TREE" }
          ]
      },
      {
          "container": "c002",
          "containerType": "timeSeries",
          "containerFileType":"csv",
          "containerFile":"public.container01.csv",
          "rowKeyAssigned":true,
          "dataAffinity":"month",
          "columnSet": [
              { "columnName": "COLUMN_TIMESTAMP", "type": "TIMESTAMP" },
              { "columnName": "COLUMN_FLAG", "type": "BOOLEAN" },
              { "columnName": "COLUMN_BLOB_DATA", "type": "BLOB" },
              { "columnName": "COLUMN_INTEGER", "type": "INTEGER" }
          ],
          "indexSet":[
              { "columnName":" COLUMN_FLAG ", "indexType": "TREE" }
          ]
      }
  ]
  ```

[Example 3] Example of a description for table partitioning

- For hash partitioning (Showing only the description for table partitioning data) >

  ``` example
    "tablePartitionInfo":{
        "type": "HASH",
        "column": "column03",
        "divisionCount": 16
    }
  ```

- For interval partitioning (Showing only the description for table partitioning data) >

  ``` example
    "tablePartitionInfo":{
        "type": "INTERVAL",
        "column": "timecolumn05",
        "intervalValue": 20,
        "intervalUnit": "DAY"
    }
  ```

- For interval-hash partitioning (Showing only the description for table partitioning data) >

  ``` example
    "tablePartitionInfo":[
         {
             "type": "INTERVAL",
             "column": "timecolumn05",
             "intervalValue": 10,
             "intervalUnit": "DAY"
         },
         {
             "type": "HASH",
             "column": "column03",
             "divisionCount": 16
         }
    ]
  ```

[Memo]
-   For interval-hash partitioning, it is necessary to describe the partitioning data under the tablePartitionInfo in the order of INTERVAL, HASH. An error occurs if the order of description is not right.


[Example 4] Example of a date accumulation container

Excerpt relevant to time interval information

  ``` example
    "timeIntervalInfo":[
      {
        "containerFile": "public.container01_2023-01-01_2023-01-02.csv",
        "boundaryValue": "2023-01-01T00:00:00.000+0000"
      }
    ]
  ```

### Row data file (binary data file)

A row data file, binary data file, is in zip format and can be created by gs_export only. No readability, and cannot be edited as well.

### Row data file (CSV data file)

A row data file, csv file, is in CSV format and describes the references to the metadata file, which defines rows, in the container data file data section.

[Memo]
-   A CSV data file is described using the UTF-8 character code.

\<CSV data file format\>

**1. Header section (1st - 2nd row)**

Header section contains data output during export. Header data is not required during import.
- Assign a "\#" at the beginning of the command to differentiate it. The format will be as follows.

  ``` example
  "#(Date and time) GridDB release version"
  "#User:(user name)"
  ```

[Example]

``` example
"#2017-10-01T17:34:36.520+0900 GridDB V4.0.00"
"#User:admin "
```

**2. Container data file data section (3rd and subsequent rows)**

Describe the references to the metadata file.
- Assign a "%" at the beginning of the command to differentiate it. The format of one row will be as follows.

  ``` example
  "%","metadata file name"
  ```

**3. Container name information section (immediately after the container data section)**

Specify the database name and the container name in a row data file.
The format varies depending on the export container type.

| Export container type               |   Format of the container name information section                  |
| -------------------------- | ---------------------------------------- |
| registration update container           | Specify a database name and a container name.          |
| date registration container |  Specify a database name and a combination of a container name and a date range. |

◆Database name and container name
- Start with the U.S. dollar sign ($) and specify a database name and a container name.

  ``` example
  "$","database name.container name"
  ```

◆Database name and a combination of container name and date range
- Start with the U.S. dollar sign ($) and specify a database name and a combination of a container name and the date range.
- The "date range" consists of a start date and an end date connected by an underscore (_). More specifically, see the following example:

  ``` example
  "$","database name.container name_YYYY-MM-dd_YYYY-MM-dd"
  ```

**4. Row data section (after the container name information section)**

This is where the main body of row data is described.

- Specify as many number of row data as demanded to be registered in a container.

  ``` example
  "value","value","value", ... (number of column definitions)
  "value","value","value", ... (number of column definitions)
      ：
      ：        //Describe the number of row cases you want to register
      ：
  ```

[Memo]
- Backslash \ and double quote " included in row data must be escaped with a backslash.
- For TIMESTAMP type, specify the value in "yyyy-MM-dd'T'HH:mm:ss.SSSZ" format.
  - Example: "2016-12-25T00:22:30.000+0900"

**4. Comments section**

The comment section can be described anywhere in the CSV data file except the header section.
-   Assign a "\#" at the beginning of the command to differentiate it.

[Memo]
-   The CSV data file of a single container data file is configured as follows.
    -     1. Header section, 2. Container data file data section, 3. Row data section
-   The CSV data file of a multi-container data file is configured as follows.
    -     1. Header section, 2. Container data file data section, 3. Row data section (multiple)

\<File name format\>

The name of the CSV data file output by the export tool is as follows.

-   When the format is a single container format
    -   <database name>.<container name>.csv
    -   The database and container name in the file name are URL-encoded. If the length of "encoded database name.encoded container name" is over 140 characters, the file name is modified as connecting 140 characters from the beginning and the sequential number.
-   When the format is a multi-container format
    -   File identifier is specified by --out option: <file identifier>.csv
    -   File identifier is omitted by --out option: YYYYMMDD_HHmmss_SSS.csv

[Example] a meta data file in CSV format ,including external object file, for Example 1

``` example
"#2017-10-01T11:19:03.437+0900  GridDB V4.0.00"
"#User:admin"
"%","public.c001_properties.json"
"$","public.c001"
"1","Tokyo"
"2","Kanagawa"
"3","Osaka"
```

  

When the data below is included in some of the rows of the CSV data file, prepare an external object file separate from the CSV data file as an external object. List the references of the external data file in the target column of the CSV file as below.
   "@data type": (file name)

-   BLOB data
    -   List "@BLOB:" + (file name) as BLOB data in the "value" section of the relevant column.
    -   The file naming section has a format which is the file name + ".blob".
    -   The binary file is located according to the rules of the file naming section.
-   Spatial data
    -   List "@GEOMETRY:" + (file name) as GEOMETRY data in the relevant "value" section.
    -   The file naming section has a format which is the file name + ".geometry".
    -   List a spatial column in the external object file.
    -   Describe using character code UTF-8.
-   Array (BOOLEAN\[\]/ STRING\[\]/ BYTE\[\]/ SHORT\[\]/ INTEGER\[\]/LONG\[\]/ FLOAT\[\]/ DOUBLE\[\]/ TIMESTAMP\[\])
    -   List "@(Data Type)_ARRAY:" + (file name) as ARRAY data in the relevant "value" section.
    -   The file naming section has a format which is the file name + ".(data type)_array".
    -   If the length of the character string exceeds 100 characters, list down array data in the external object file.
    -   Describe using character code UTF-8.
-   Character string data
    -   List "@STRING:" + (file name) as STRING data in the relevant "value" section.
    -   The file naming section has a format which is the file name + ".string".
    -   If the length of the character string exceeds 100 characters, or if it includes a line return (\r), list down string data in the external object file.
    -   Describe using character code UTF-8.

When an external object file is exported, the external object file name is created in accordance with the following rules during export.
-   When the format is a single container format
    -   \<Database name\>.\<container name\>_\<row no.\>_\<column no.\>.\<data type\>
    -   The row no. and column no. shows the sequence no. of the container data and they are numbered starting from 0.
    -   Example) When the column of an container is a Byte array, an external object file name is as follows: database name.container name_ROW number_COLUMN number .byte_array.
-   When the format is a multi-container format
    -   File identifier specified in the --out option \<File identifier\>_\<database name\>_\<container name\>_\<row no.\>_\<column no.\>.\<data type\>
    -   File identifier omitted in --out option \<Date and time\>_\<database name\>_\<container name\>_\<row no.\>_\<column no.\>.\<data type\>
-   The database and container name in the file name are URL-encoded. If the length of "encoded database name.encoded container name" is over 140 characters, the file name is modified as connecting 140 characters from the beginning and the sequential number.

For import purposes, any file name can be used for the external object file. List down the CSV data file with a file name of any data type in the relevant column.

[Example] Naming example of an external object file

``` example
//When a collection (colb) having a BYTE array in the 3rd column is exported
  
10月  4 12:51 2017 public.colb.csv
10月  4 12:51 2017 public.colb_0_3.byte_array
10月  4 12:51 2017 public.colb_1_3.byte_array
10月  4 12:51 2017 public.colb_2_3.byte_array
10月  4 12:51 2017 public.colb_3_3.byte_array
10月  4 12:51 2017 public.colb_4_3.byte_array
10月  4 12:51 2017 public.colb_properties.json
```

    

[Example] Description of an external object file in a single container data file is shown below.

- Metadata file public.col01_properties.json

  ``` json
  {
          "version": "4.0.0",
          "container": "col01",
          "containerFile": "public.col01.csv",
          "containerFileType": "csv",
          "containerType": "COLLECTION",
          "columnSet": [
              { "columnName": "name","type": "string"  },
              { "columnName": "status", "type": "boolean"},
              { "columnName": "count", "type": "long" },
              { "columnName": "lob", "type": "byte[]"
              }
          ],        
          "indexSet": [
              {
                  "columnName": "name",
                  "type": "TREE"
              },
              {
                  "columnName": "count",
                  "type": "TREE"
              }
          ],
          "rowKeyAssigned": true
  }
  ```

- CSV data file public.col01.csv

  ```example
  "#2017-10-01T19:41:35.320+0900  GridDB V4.0.00"
  "#User:admin"
  "%","public.col01_properties.json"  
  "$","public.col01"
  "name02","false","2","@BYTE_ARRAY:public.col01_0_3.byte_array"
  ```

- External object file public.col01_03.byte_array

  ``` example
  1,10,15,20,40,70,71,72,73,74
  ```


## Avoid data deviation

Data deviation can happen when registering a large amount of data into containers.
To avoid that, these parameters are added to metadata file when exporting/importing containers those are interval partition (timeseries or collection) AND the partitioning key is TIMESTAMP:

| Item                        | Description                                                                                         |
| --------------------------- | --------------------------------------------------------------------------------------------------- |
| intervalWorkerGroup         | Specify what worker group is the container on; corresponding to PARTITION_INTERVAL_WORKER_GROUP     |
| intervalWorkerGroupPosition | Specify the position of the worker group; corresponding to PARTITION_INTERVAL_WORKER_GROUP_POSITION |

- Export:
  System will get all the data from #tables table. If the data contains PARTITION_INTERVAL_WORKER_GROUP, PARTITION_INTERVAL_WORKER_GROUP_POSITION it will set their value for the corresponding parameters in the metadata file (<database_name>.<container_name>\_properties.json).

- Import:
  System will read from metadatafile (<database_name>.<container_name>\_properties.json)
  - If intervalWorkerGroup's values exists: Adds `WITH (PARTITION_INTERVAL_WORKER_GROUP = <value of intervalWorkerGroup>)` to `CREATE TABLE` statement.
  - If both intervalWorkerGroup and intervalWorkerGroupPosition's values exists: Adds `WITH (PARTITION_INTERVAL_WORKER_GROUP = <value of intervalWorkerGroup> AND PARTITION_INTERVAL_WORKER_GROUP_POSITION = <value of intervalWorkerGroupPosition>)` to `CREATE TABLE` statement
  - If intervalWorkerGroupPosition's value is null or intervalWorkerGroup's value is null:
    - System will catch the exception returned from server.
  - If tablePartitionInfo does not exist but intervalWorkerGroupPosition's value or intervalWorkerGroup's value is not null:
    - System will throw exception with error message: Interval partition table must be set when (interval_worker_group or interval_worker_group_position) are specified

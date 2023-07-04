# GridDB Export/Import tools

## Overview

The GridDB export/import tools, to recover a database from local damages or the database migration process, save/recovery functions are provided in the database and container unit.

## Operating environment

Building and program execution are checked in the environment below.

    OS: CentOS 7.9 (x64)
    Java: Java™ SE Development Kit 8
    GridDB Server: V4.6.1/V5.3.0 CE (Community Edition)
    GridDB Java Client: V4.6.1/V5.3.0 CE (Community Edition)
    GridDB JDBC: V4.6.0/V5.3.0 CE (Community Edition)
    
## Quick start - Build and Run

### Preparations

- Install GridDB Server with RPM or DEB package.

### Build

Run the make command like the following:
```
$ cd expimp-ce
$ ./gradlew shadowJar
```
and the following file is created under `expimp-ce/build/libs/` folder. 

```
griddb-expimp-ce-all.jar
```

### Run GridDB Export/Import

- Configure properties follow your cluster. An example as below:

  ```
  $ vi bin/gs_expimp.properties
  ######################################################################
  # gs_import/gs_export Properties
  #
  #
  ######################################################################
  clusterName=myCluster
  mode=MULTICAST
  hostAddress=239.0.0.1
  hostPort=31999
  jdbcAddress=239.0.0.1
  jdbcPort=41999
  notificationProvider.url=
  notificationMember=
  jdbcNotificationMember=
  
  ```

- Import sample data
```
$ cd bin
$ ./gs_import -u admin/[password] -d ../impSample/collection --all
$ ./gs_import -u admin/[password] -d ../impSample/timeseries --all
$ ./gs_import -u admin/[password] -d ../impSample/arrayData --all
```

- Export sample data
```
$ cd bin
$ ./gs_export -u admin/[password] -d outCollection -c c001
$ ./gs_export -u admin/[password] -d outTimeseries -c t001
$ ./gs_export -u admin/[password] -d outArrayData -c colb
```

## Document

  Refer to the file below for more detailed information.  
  - [Specification (en)](Specification_en.md)
  - [Specification (ja)](Specification_ja.md)


## Community
  * Issues  
    Use the GitHub issue function if you have any requests, questions, or bug reports. 
  * PullRequest  
    Use the GitHub pull request function if you want to contribute code. You'll need to agree GridDB Contributor License Agreement(CLA_rev1.1.pdf). By using the GitHub pull request function, you shall be deemed to have agreed to GridDB Contributor License Agreement.

## License
  The GridDB Export/Import source license is Apache License, version 2.0.  


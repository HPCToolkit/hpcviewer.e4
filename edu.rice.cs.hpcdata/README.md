# hpcdata Plugin

hpcdata is an independent plugin part of hpcviewer written purely in Java to ensure portability across platforms.
No Eclipse SWT or JFace can be used in this project.

## Requirements
- Java 11 or newer
- External libraries:
  - com.graphbuilder
  - org.eclipse.collections
  - ca.odell.glazedlists

To build as an independent application:

- Go to `edu.rice.cs.hpcapp` project directory
- Run `./build.sh` script 
- copy `hpcdata.tgz` to the installation directory
- untar `hpcdata.tgz`
- Optionally, change the permission of `hpcdata.sh` (like `chmod ugo+x hpcdata.sh`)

# How to run

Syntax: 

    hpcdata.sh [-o output_file] experiment_database

# Source code

Currently the source code of hpcdata has only one main file:

    src/edu/rice/cs/hpcdata/framework/PrintData.java

Or you can see at
https://github.com/HPCToolkit/hpcviewer.e4/blob/master/edu.rice.cs.hpc.data/src/edu/rice/cs/hpcdata/framework/PrintData.java

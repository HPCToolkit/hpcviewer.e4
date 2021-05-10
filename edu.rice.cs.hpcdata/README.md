# hpcdata Plugin

hpcdata is an independent plugin part of hpcviewer written purely in Java to ensure portability across platforms.

## Requirements
- Java 11 or newer
- Apache Ant. You can install Apache Ant via `spack install ant`

To build as an independent plugin:

- Go to `script` directory
- Type `ant -buildfile hpcdata.xml`
- copy `hpcdata.zip` to the installation directory
- unzip `hpcdata.zip`
- Optionally, change the permission of `hpcdata.sh` (like `chmod ugo+x hpcdata.sh`)

# How to run

Syntax: 

    hpcdata.sh [-o output_file] experiment_database

# Source code

Currently the source code of hpcdata has only one main file:

    src/edu/rice/cs/hpcdata/framework/PrintData.java

Or you can see at
https://github.com/HPCToolkit/hpcviewer.e4/blob/master/edu.rice.cs.hpc.data/src/edu/rice/cs/hpcdata/framework/PrintData.java

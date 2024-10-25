<!--
SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project

SPDX-License-Identifier: CC-BY-4.0
-->

# hpcdata project

hpcdata is an independent project part of hpcviewer written purely in Java to ensure portability across platforms.
No GUI, Eclipse SWT or JFace can be used in this project.

## Requirements
- Java 11 or newer
- External libraries:
  - [com.graphbuilder](https://gitlab.com/hpctoolkit/graphbuilder)
  - org.eclipse.collections
  - ca.odell.glazedlists
  - org.yaml.snakeyaml
  - org.apache.commons.math3

## Building as an independent application:

- Run `mvn clean package` on hpcviewer project directory
- Go to  `edu.rice.cs.hpcdata.app/scripts` directory.
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


<!--
SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project

SPDX-License-Identifier: CC-BY-4.0
-->

The repository contains several modules, each is an independent Eclipse project:

- edu.rice.cs.hpcbase : contains common API used by other modules
- edu.rice.cs.hpcdata : OSGI wrapper for hpcdata jar file
- edu.rice.cs.hpcdata.merge : pure Java API to merge different databases. It has no GUI. 
- edu.rice.cs.hpcfilter : API and UI to filter CCT nodes
- edu.rice.cs.hpcgraph : library to plot graphs
- edu.rice.cs.hpclocal : pure Java library to open and read local databases 
- edu.rice.cs.hpclog : library to output logs to a file or standard output 
- edu.rice.cs.hpcmerge : library and GUI to merge databases
- edu.rice.cs.hpcmetric : library and GUI to manage performance metrics 
- edu.rice.cs.hpcremote : library and GUI to access remote databases
- edu.rice.cs.hpcremote.client : OSGI wrapper for hpcclient
- edu.rice.cs.hpcremote.common : OSGI wrapper for hpccommonclientserver 
- edu.rice.cs.hpcremote.jsch : OSGI wrapper for jsch library
- edu.rice.cs.hpcsetting : library to open, read and update hpcviewer preferences 
- edu.rice.cs.hpctoolcontrol : library/UI to display background jobs
- edu.rice.cs.hpctraceviewer.config : library and GUI to set hpctraceviewer's preferences 
- edu.rice.cs.hpctraceviewer.data : library to read and gather data of traces
- edu.rice.cs.hpctraceviewer.filter : library to filter execution contexts to be displayed 
- edu.rice.cs.hpctraceviewer.ui : main library to display trace view
- edu.rice.cs.hpctree : library for scalable tree table
- edu.rice.cs.hpcviewer.feature : Eclipse feature specification
- edu.rice.cs.hpcviewer.product : hpcviewer product specification
- edu.rice.cs.hpcviewer.ui : main library for hpcviewer

<!--
SPDX-FileCopyrightText: 2020-2024 Rice University
SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project

SPDX-License-Identifier: CC-BY-4.0
-->

# Overview

`hpcviewer` is the presentation layer of [HPCToolkit](https://gitlab.com/hpctoolkit/hpctoolkit) which is a suite of tools
for measurement and analysis of program performance.

One of the main advantages of `hpcviewer` is its ability to handle massive amounts of performance data, a necessity for modern HPC applications that run on thousands of cores or GPUs. The GUI is designed to efficiently process and present this data, allowing users to focus on analyzing performance without being overwhelmed by the sheer volume of information. Its hierarchical visualization techniques make it easier to manage complexity, especially when dealing with deeply nested function calls or multi-threaded programs.

Another key advantage is the integration between the performance data and source code. This seamless connection allows developers to not only see where inefficiencies occur but also immediately access the code responsible for the performance issues. 

`hpcviewer` can run on multiple operating systems including Linux (x86-64, IBM Power 64 and ARM 64), macOS (Intel and Silicon), and Windows (x86-64). 



## Requirements

* Java 17 or newer.
  Can be downloaded via [Spack](https://github.com/spack/spack) 
  or from [Oracle](https://www.oracle.com/java/technologies/javase-downloads.html)
  or [Adoptium](https://adoptium.net/temurin/releases)
* Linux: GTK+ 3.20 or newer.

## Installation

1. Go to the [hpcviewer latest release]
2. Select the appropriate binary to download in **Packages** Section

See [`INSTALL.md`](INSTALL.md) for further details.

## Documentation

See the [online manual](https://hpctoolkit.gitlab.io/hpcviewer/).


# Contributing

See [contributing](CONTRIBUTING.md) file for details.


# License

This source distribution as a whole is licensed under the [`LICENSE`](./LICENSE). This source distribution follows [REUSE Specification] Version 3 to declare copyright and licensing at file granularity.

[reuse specification]: https://reuse.software/spec/
[hpcviewer latest release]: [https://gitlab.com/hpctoolkit/hpcviewer/-/releases/permalink/latest]

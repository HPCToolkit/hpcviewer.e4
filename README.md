<!--
SPDX-FileCopyrightText: 2020-2024 Rice University
SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project

SPDX-License-Identifier: CC-BY-4.0
-->

# Overview

HPCViewer is the presentation layer of [HPCToolkit](https://gitlab.com/hpctoolkit/hpctoolkit), a suite of tools for measurement of program performance. HPCViewer allows you to open HPCToolkit performance databases and visualize the performance of an application.

## Features

- Hierarchical "top-down" and "bottom-up" performance analysis based on application calling context.
- Source code pane for viewing the application source code for function or line.
- Trace view for analyzing time-oriented performance.
- Runs well even when viewing TBs of performance data.
- Portable across Linux, MacOS, and Windows platforms.

## Requirements

- Java 17 or newer. Java can be downloaded from [Adoptium](https://adoptium.net/temurin/releases) or [Oracle](https://www.oracle.com/java/technologies/javase-downloads.html), or installed via [Spack](https://spack.io).
- Linux: GTK+ 3.20 or newer.

## Installation

See [`INSTALL.md`] for platform-specific installation instructions and building from source.

## Documentation

See the [manual](https://hpctoolkit.gitlab.io/hpcviewer/).

# Contributing

See [`CONTRIBUTING.md`] for details.

# License

This source distribution as a whole is licensed under the [`LICENSE`](./LICENSE). This source distribution follows [REUSE Specification] Version 3 to declare copyright and licensing at file granularity, the individual license texts are provided in the `LICENSES/` subdirectory.

[reuse specification]: https://reuse.software/spec/
[`contributing.md`]: CONTRIBUTING.md
[`install.md`]: INSTALL.md

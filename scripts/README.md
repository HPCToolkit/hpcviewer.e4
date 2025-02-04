<!--
SPDX-FileCopyrightText: 2020-2024 Rice University
SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project

SPDX-License-Identifier: CC-BY-4.0
-->

# Installing on Linux (recommended for laptop/desktop)

1. If Java 17 or newer (Java 21 recommended) is not installed, install a recent Java from [Adoptium](https://adoptium.net/temurin/releases/?version=32&os=linux) or via [Spack](https://spack.io).
1. Extract the downloaded tarball to a local directory:
   ```console
   $ tar xzf hpcviewer-linux.gtk.*.tgz
   ```
1. Run the install script:
   ```console
   $ cd hpcviewer/
   $ ./install.sh /my/install/dir/  # e.g. ~/hpcviewer/
   ```
1. Run `/my/install/dir/hpcviewer`, or add `/my/install/dir` to your `PATH`.


# Notes

* By default, the configuration files are located in 
  `$USER/.hpctoolkit/hpcviewer/<platform>/`
* To reset the configuration, you need to remove this directory.

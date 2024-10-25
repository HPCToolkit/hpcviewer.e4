<!--
SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project

SPDX-License-Identifier: CC-BY-4.0
-->

# Installing hpcviewer

## Installing on MacOS

1. If Java 17 or newer (Java 21 recommended) is not installed, install a recent Java from [Adoptium](https://adoptium.net/temurin/releases/?version=21&os=mac) (recommended):
   - [Java 21 JDK MacOS Apple Silicon](https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.4%2B7/OpenJDK21U-jdk_aarch64_mac_hotspot_21.0.4_7.pkg)
   - [Java 21 JDK MacOS Intel](https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.4%2B7/OpenJDK21U-jdk_x64_mac_hotspot_21.0.4_7.pkg)
1. Download the HPCViewer binary package matching your architecture:
   - **Apple Silicon** (M1/M2/M3) MacOS: [hpcviewer-macosx.cocoa.aarch64.dmg](https://gitlab.com/hpctoolkit/hpcviewer/-/releases/permalink/latest/downloads/hpcviewer-macosx.cocoa.aarch64.dmg)
   - **Intel** MacOS: [hpcviewer-macosx.cocoa.x86_64.dmg](https://gitlab.com/hpctoolkit/hpcviewer/-/releases/permalink/latest/downloads/hpcviewer-macosx.cocoa.x86_64.dmg)
1. Double-click the downloaded `*.dmg` file.
1. Drag the `hpcviewer.app` icon to the "Applications" icon.
1. Run `hpcviewer.app` from the Applications menu.

## Installing on Windows

1. If Java 17 or newer (Java 21 recommended) is not installed, install a recent Java from [Adoptium](https://adoptium.net/temurin/releases/?version=21&os=windows) (recommended):
   - [Java 21 JDK Windows x64](https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.4%2B7/OpenJDK21U-jdk_x64_windows_hotspot_21.0.4_7.msi)
1. Download the HPCViewer binary package matching your architecture:
   - Intel or AMD x86: [hpcviewer-win32.win32.x86_64.zip](https://gitlab.com/hpctoolkit/hpcviewer/-/releases/permalink/latest/downloads/hpcviewer-win32.win32.x86_64.zip)
1. Double-click the downloaded `*.zip` file to open it.
1. Extract the `hpcviewer` folder inside to a local folder (e.g. `Desktop`).
1. Run `hpcviewer.exe` in the extracted `hpcviewer` folder.

## Installing on Linux (recommended for laptop/desktop)

1. If Java 17 or newer (Java 21 recommended) is not installed, install a recent Java from [Adoptium](https://adoptium.net/temurin/releases/?version=32&os=linux) or via [Spack](https://spack.io).
1. Download the HPCViewer binary package matching your architecture:
   - Intel or AMD x86: [hpcviewer-linux.gtk.x86_64.tgz](https://gitlab.com/hpctoolkit/hpcviewer/-/releases/permalink/latest/downloads/hpcviewer-linux.gtk.x86_64.tgz)
   - ARM: [hpcviewer-linux.gtk.aarch64.tgz](https://gitlab.com/hpctoolkit/hpcviewer/-/releases/permalink/latest/downloads/hpcviewer-linux.gtk.aarch64.tgz)
   - IBM PowerPC: [hpcviewer-linux.gtk.ppc64le.tgz](https://gitlab.com/hpctoolkit/hpcviewer/-/releases/permalink/latest/downloads/hpcviewer-linux.gtk.ppc64le.tgz)
1. Extract the downloaded tarball to a local directory:
   ```console
   $ tar xzf hpcviewer-linux.gtk.*.tgz
   ```
1. Run the install script:
   ```console
   $ cd hpcviewer/
   $ ./install /my/install/dir/  # e.g. ~/hpcviewer/
   ```
1. Run `/my/install/dir/hpcviewer`, or add `/my/install/dir` to your `PATH`.

## Installing via Spack (recommended for headless servers)

HPCViewer is available via [Spack](https://spack.io), simply run:

```console
$ spack install hpcviewer
$ spack load hpcviewer
```

See the [Spack User's Manual] for instructions on how to set up Spack for your system and use it to install packages including HPCViewer.

# Building HPCViewer from source

## Build via command line (Maven)

- Download and install Maven (if not available on the systems) at https://maven.apache.org/

### On Posix-based platform with Bash shell (Linux and MacOS),

Run the build script from the project root:

```console
$ ./build.sh
```

The script generates five `hpcviewer-<release>-<platform>.[zip|tgz]` files: Windows, Mac (x86_64 and Arm), and Linux (x86_64, ppcle64, and Arm).

- For MacOS: `unzip` the appropriate `.zip` file for your platform and run the `hpcviewer` script contained inside.
- For Linux: Follow the install instructions for Linux above with the appropriate `.tgz` file for your platform.

### On Windows

Build directly with the Maven script:

```console
$ mvnw.cmd clean package
```

This will compile and create hpcviewer packages for 4 platforms: Linux x86_64 and ppcle64, Windows and MacOS with Eclipse 4.30 (the default). Example of the output:

```
...
[INFO] Building zip: <hpcviewer.e4>/edu.rice.cs.hpcviewer.product/target/products/hpcviewer-win32.win32.x86_64.zip
```

Extract `hpcviewer-win32.win32.x86_64.zip` to a local folder, then run the `hpcviewer.exe` contained inside. (It is recommended to keep your development HPCViewer install separate from a release install.)

## Building via Eclipse IDE

Requirements:

- Recommended: [Eclipse 2023.12 RCP](https://www.eclipse.org/downloads/packages/release/2023-12/r/eclipse-ide-rcp-and-rap-developers) or newer.
- Warning: May not work properly with older versions of Eclipse.

### Getting the source code into the Eclipse IDE

- `git clone https://gitlab.com/hpctoolkit/hpcviewer` to your local system
- Start Eclipse
- Open the Import window via the menu File > Import
- Select the import wizard General > Existing Projects into Workspace and click Next >
- In Select root directory, select the directory where you have downloaded (the Git root)
- In the Options section of the window, activate Search for nested projects
- Click Finish

### Activating the target plarform

To run hpcviewer, it requires Eclipse bundles and some external libraries such as Nebula NatTable, Eclipse Collections, JCraft and SLF4J. The set of bundles that are available is defined by the bundles in the Eclipse workspace, and additionally the bundles in the active target platform The first Eclipse starts after the installation, the target platform only includes the bundles that are installed in the workspace which doesn't include the external libraries.

The bundles that hpcviewer needs are defined in a custom target platform definition project, which is located in the `target.platform` directory:

- Open file `target-platform.target` in `target.platform project`.
- Click the "Set as Active Target Platform" link at the top-right panel.

### Run the app

- Open product configuration `hpcviewer.product` at `edu.rice.cs.hpcviewer.product`
- To run: Click `Launch an Eclipse application`

## Coding style

Recommended coding and formatting style:

- [Sonar source quality standards](https://www.sonarsource.com/java/)
- [Google Java style guide](https://google.github.io/styleguide/javaguide.html)

[spack user's manual]: https://spack.readthedocs.io/

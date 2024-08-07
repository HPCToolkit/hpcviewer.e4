# hpcviewer

hpcviewer is the presentation layer of HPCToolkit which is a suite of tools
for measurement and analysis of program performance.
It interactively presents program performance in a top-down fashion and also 
visualizes trace data generated by hpcrun if the flag ```-t``` is specified. 
For static linked program, the variable environment ```HPCRUN_TRACE``` has to be set. 

## General Requirements

* Java 17 or newer.
  Can be downloaded via [Spack](https://github.com/spack/spack) 
  or from [Oracle](https://www.oracle.com/java/technologies/javase-downloads.html)
  or [Adoptium](https://adoptium.net/temurin/releases)
* Linux: GTK+ 3.20 or newer.

If there is no GTK+ 3.20 or newer installed in the system, you may install it via `spack`:

```
spack install gtkplus
spack load gtkplus
```

## How to build and run via command line (Maven)

* Download and install Maven (if not available on the systems) at https://maven.apache.org/
  * Recommended: install via spack
  	`spack install maven; spack load maven`  
  	
### On Posix-based platform with Bash shell (Linux and MacOS), 

Run the build script from the project root:

```
    ./build.sh
```
   The script generates five `hpcviewer-<release>-<platform>.[zip|tgz]` files:
    Windows, Mac (x86_64 and Arm), and Linux (x86_64, ppcle64, and Arm).
  * `untar` or `unzip` the file based according to the platform. 
  
  * ONLY for Linux platform, need to run the installation script:

```
./install.sh <directory>
```
  where `<directory>` is the installation root for hpcviewer binary. 
  
  
### On Windows 

Build directly with the Maven script:

```
  mvnw.cmd clean package
```
  This will compile and create hpcviewer packages for 4 platforms: Linux x86_64 and ppcle64, Windows and Mac
  with Eclipse 4.30 (the default).
  Example of the output:

```
...
[INFO] Building zip: <hpcviewer.e4>/edu.rice.cs.hpcviewer.product/target/products/edu.rice.cs.hpcviewer-win32.win32.x86_64.zip
```
  Unzip `edu.rice.cs.hpcviewer-win32.win32.x86_64.zip` to another folder. 
  It isn't recommended to overwrite the existing folder.


## How to build and run via Eclipse IDE

Requirements:

* Recommended: [Eclipse 2023.12 RCP](https://www.eclipse.org/downloads/packages/release/2023-12/r/eclipse-ide-rcp-and-rap-developers) or newer. 
* Warning: May not work properly with older versions of Eclipse. 

Recommended:
* Source code for [hpcdata](https://gitlab.com/hpctoolkit/hpcdata)
* Source code for [graphbuilder (math parser)](https://gitlab.com/hpctoolkit/graphbuilder)
* Source code for remote database:
  * [hpcclient-java](https://gitlab.com/hpctoolkit/hpcclient-java)
  * [hpcclient common](https://gitlab.com/hpctoolkit/hpcclientservercommon/)
 
```
  git clone https://gitlab.com/hpctoolkit/hpcdata
  git clone https://gitlab.com/hpctoolkit/graphbuilder
```


### Getting the source code into the Eclipse IDE

* Start Eclipse
* Open the Import window via the menu File > Import
* Select the import wizard General > Existing Projects into Workspace and click Next >
* In Select root directory, select the directory where you have downloaded (the Git root)
* In the Options section of the window, activate Search for nested projects
* Click Finish

### Activating the target plarform

To run hpcviewer, it requires Eclipse bundles and some external libraries such as Nebula NatTable, Eclipse Collections, JCraft and SLF4J.
The set of bundles that are available is defined by the bundles in the Eclipse workspace, and additionally the bundles in the active target platform
The first Eclipse starts after the installation, the target platform only includes the bundles that are installed in the workspace which doesn't include the external libraries.

The bundles that hpcviewer needs are defined in a custom target platform definition project, which is located in the `target.platform` directory:

* Open file `target-platform.target` in `target.platform project`.
* Click the "Set as Active Target Platform" link at the top-right panel.

### Run the app

* Open product configuration `hpcviewer.product` at `edu.rice.cs.hpcviewer.product`
* To run: Click `Launch an Eclipse application`


## Coding style

Recommended coding and formatting style:
* [Sonar source quality standards](https://www.sonarsource.com/java/)
* [Google Java style guide](https://google.github.io/styleguide/javaguide.html)

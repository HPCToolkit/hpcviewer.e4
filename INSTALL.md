# Installating hpcviewer

## Detailed instructions for MacOS platforms
1. If Java 17 or newer is not installed, it is recommended to install via [Adoptium](https://adoptium.net/temurin/releases/?version=17&os=mac) website
   - [Java 21 JDK MacOS Apple Silicon](https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.4%2B7/OpenJDK21U-jdk_aarch64_mac_hotspot_21.0.4_7.pkg)
   - [Java 21 JDK MacOS Intel](https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.4%2B7/OpenJDK21U-jdk_x64_mac_hotspot_21.0.4_7.pkg)
2 Recommended: download file with `*.dmg` extension:
   - **Apple Silicon** (M1/M2/M3) MacOS: [hpcviewer-macosx.cocoa.aarch64.dmg](https://gitlab.com/hpctoolkit/hpcviewer/-/releases/permalink/latest/downloads/hpcviewer-macosx.cocoa.aarch64.dmg)
   - **Intel** MacOS: [hpcviewer-macosx.cocoa.x86_64.dmg](https://gitlab.com/hpctoolkit/hpcviewer/-/releases/permalink/latest/downloads/hpcviewer-macosx.cocoa.x86_64.dmg) 
3 Double-click the downloaded `*.dmg` file
4 Drag the `hpcviewer.app` icon to the `Applications` icon


## Detailed instructions for Windows x86 platforms
1. If Java 17 or newer is not installed, it is recommended to install via [Adoptium](https://adoptium.net/temurin/releases/?version=17&os=windows) website
   - [Java 21 JDK Windows x64](https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.4%2B7/OpenJDK21U-jdk_x64_windows_hotspot_21.0.4_7.msi)
2. Download [hpcviewer-win32.win32.x86_64.zip](https://gitlab.com/hpctoolkit/hpcviewer/-/releases/permalink/latest/downloads/hpcviewer-win32.win32.x86_64.zip)
3. Extract the downloaded file by double-clicking the downloaded file
4. Drag the `hpcviewer` folder icon to another folder such as `Desktop` or `Program`


## Detailed instructions for Linux platforms
1. If Java 17 or newer is not installed, install Java JRE or JDK from `spack` or from [Adoptium](https://adoptium.net/temurin/releases/?version=17&os=linux) website
2. Download the appropriate hpcviewer binary:
   - Intel or AMD x86: [hpcviewer-linux.gtk.x86_64.tgz](https://gitlab.com/hpctoolkit/hpcviewer/-/releases/permalink/latest/downloads/hpcviewer-linux.gtk.x86_64.tgz) 
   - ARM: [hpcviewer-linux.gtk.aarch64.tgz](https://gitlab.com/hpctoolkit/hpcviewer/-/releases/permalink/latest/downloads/hpcviewer-linux.gtk.aarch64.tgz)
   - IBM PowerPC: [hpcviewer-linux.gtk.ppc64le.tgz](https://gitlab.com/hpctoolkit/hpcviewer/-/releases/permalink/latest/downloads/hpcviewer-linux.gtk.ppc64le.tgz)
3. Untar the downloaded file:
   ```
   tar xf <downloaded_hpcviewer_file>
   ```
4. Go to the directory and run the `install.sh` script:
   ```
   cd hpcviewer
   ./install.sh <installation_directory>
   ```


# Building hpcviewer from the source

## Build via command line (Maven)

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


## Building via Eclipse IDE

Requirements:

* Recommended: [Eclipse 2023.12 RCP](https://www.eclipse.org/downloads/packages/release/2023-12/r/eclipse-ide-rcp-and-rap-developers) or newer. 
* Warning: May not work properly with older versions of Eclipse. 


### Getting the source code into the Eclipse IDE

* Git clone hpcviewer repository https://gitlab.com/hpctoolkit/hpcviewer
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

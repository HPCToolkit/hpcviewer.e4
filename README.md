# hpcviewer.e4

This is a pilot project of hpcviewer based on Eclipse 4, rewriting mostly from scratch. Some parts are portable without modification (like **hpcdata** plugin), many are heavily modified.
Once stable, we may integrate into HPCToolkit spack build system.

The goals:

* Can work the latest version of Eclipse and newer version of Java (11 or newer). **done**
* Can work with 4 platforms: Win32, Mac, Linux x86_64 and Linux ppcle64. **done**
* Can be built easily with a command line (Maven) from Linux, Mac and Windows. **partially done**
* Can integrate smoothly between `hpcviewer` and `hpctraceviewer`. **partially done**
* Can be automatically tested.

## How to build and run via command line (Maven)

* Download and install Maven (if not available on the systems) at https://maven.apache.org/
* Recommended: Maven 3.6.x
* Recommended: install Maven via spack: `spack install maven`
* type `mvn clean package`
* This will compile and create hpcviewer packages for 4 platforms: Linux x86_64 and ppcle64, Windows and Mac. Example of the output:
```
...
[INFO] --- tycho-p2-director-plugin:1.6.0:archive-products (archive-prodcuts) @ edu.rice.cs.hpcviewer ---
[INFO] Building tar: <hpcviewer.e4>/edu.rice.cs.hpcviewer.product/target/products/edu.rice.cs.hpcviewer-linux.gtk.x86_64.tar.gz
[INFO] Building tar: <hpcviewer.e4>/edu.rice.cs.hpcviewer.product/target/products/edu.rice.cs.hpcviewer-linux.gtk.ppc64le.tar.gz
[INFO] Building zip: <hpcviewer.e4>/edu.rice.cs.hpcviewer.product/target/products/edu.rice.cs.hpcviewer-win32.win32.x86_64.zip
[INFO] Building zip: <hpcviewer.e4>/edu.rice.cs.hpcviewer.product/target/products/edu.rice.cs.hpcviewer-macosx.cocoa.x86_64.zip
[INFO] ------------------------------------------------------------------------
```
* Unzip (or untar) the product file at `edu.rice.cs.hpcviewer.product/target/products/edu.rice.cs.hpcviewer-...` according to the platform.

## How to build and run via Eclipse IDE

### Getting the source code into the Eclipse IDE

Requirements:

* Recommended: [Eclipse 2019.03](https://www.eclipse.org/downloads/packages/release/2019-03/r/eclipse-ide-rcp-and-rap-developers). May not work with older versions. For the newer versions (2019.06 and newer), hpcviewer still works but due to high-definition pixels support, the mapping between pixel and procedure names do not work properly (especially on Mac OS with retina display)

To import the source code into Eclipse IDE:

* Start Eclipse
* Open the Import window via the menu File > Import
* Select the import wizard General > Existing Projects into Workspace and click Next >
* In Select root directory, select the directory where you have downloaded (the Git root)
* In the Options section of the window, activate Search for nested projects
* Click Finish

to run:

* Open product configuration `hpcviewer.product` at `edu.rice.cs.hpcviewer.product`
* To run: Click `Launch an Eclipse application`
* To export: Click `Eclipse product export wizard`


## Coding style

Recommended coding and formatting style: [https://google.github.io/styleguide/javaguide.html](Google Java style guide).

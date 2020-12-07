# hpcviewer.e4


## General Requirements

* Java 8 to 14. Currently it does not work with Java 15. 
* Linux: GTK+ 3.20 or newer.
To check installed GTK version on Red Hat distributions:
```
rpm -q gtk3
```
On Debian-based distributions:
```
dpkg -l  libgtk-3-0
apt-cache policy libgtk-3-0
```
If there is no GTK+ 3.20 or newer installed in the system, you may install it via `spack`:
```
spack install gtkplus
spack load gtkplus
```

## How to build and run via command line (Maven)

* Download and install Maven (if not available on the systems) at https://maven.apache.org/
* Recommended: install via spack
  `spack install maven; spack load maven`
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

* Recommended: [Eclipse 2020.06](https://www.eclipse.org/downloads/packages/release/2020-06/r/eclipse-ide-rcp-and-rap-developers). 
* Warning: May not work properly with older or newer versions of Eclipse. 

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

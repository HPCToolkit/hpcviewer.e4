# hpcviewer.e4

This is a pilot project of hpcviewer based on Eclipse 4, rewriting mostly from scratch. Some parts are portable without modification (like **hpcdata** plugin), many are heavily modified.
Once stable, we'll merge to HPCToolkit repository.

The goals:

* Can work the latest version of Eclipse and newer version of Java (9 or newer).
* Can work with 4 platforms: Win32, Mac, Linux x86_64 and Linux ppcle64
* Can be built easily with a command line (Maven) from Linux, Mac and Windows.

## How to build and run

### Via command line (Maven)

* Download and install Maven (if not available on the systems) at https://maven.apache.org/
* Recommended: Maven 3.6.x
* type `mvn clean package`
* Unzip the file at `edu.rice.cs.hpcviewer.product/target/products/`

### Via Eclipse

* Recommended: [Eclipse 2020.03](https://www.eclipse.org/downloads/packages/release/2020-03/r/eclipse-ide-rcp-and-rap-developers). May not work with older version.
* Open product configuration `hpcviewer.product` at `edu.rice.cs.hpcviewer.product`
* To run: Click `Launch an Eclipse application`
* To export: Click `Eclipse product export wizard`

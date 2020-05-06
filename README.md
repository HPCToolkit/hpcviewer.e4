# hpcviewer.e4
This is a pilot project of hpcviewer based on Eclipse 4, writing from scratch.
Once stable, we'll merge to HPCToolkit repository.

This project is a pilot project of hpcviewer with the following criteria:

* Can work the latest version of Eclipse
* Can work with 4 platforms: Win32, Mac, Linux x86_64 and Linux ppcle64
* Can be built easily with a command line (Maven) from Linux, Mac and Windows.

## How to build and run

### Via command line (Maven)

* Download and install Maven (if not available on the systems) at https://maven.apache.org/
* Recommended: Maven 1.8.x
* type `mvn clean package`
* Unzip the file at `edu.rice.cs.hpcviewer.product/target/products/`

### Via Eclipse

* Recommended: Eclipse 2020.03. May not work with older version.
* Open product configuration `hpcviewer.product` at `edu.rice.cs.hpcviewer.product`
* To run: Click `Launch an Eclipse application`
* To export: Click `Eclipse product export wizard`

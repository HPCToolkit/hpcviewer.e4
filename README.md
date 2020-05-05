# hpcviewer.e4
Eclipse 4 version of hpcviewer

This project is a pilot project of hpcviewer with the following criteria:

* Can work the latest version of Eclipse
* Can work with 4 platforms: Win32, Mac, Linux x86_64 and Linux ppcle64
* Can be built with a command line (Maven)

## How to build

### Via comman line (Maven)

* Download and install Maven (if not available on the systems)
* type `mvn clean package`
* Unzip the file at `edu.rice.cs.hpcviewer.product/target/products/`

### Via Eclipse

* Open product configuration at `edu.rice.cs.hpcviewer.product`
* Click `Launch an Eclipse application`

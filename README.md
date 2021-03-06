<p align="center">
  <img src="openBD_logo_788x150px.png">
</p>

OpenBD was the worlds first truly open source and free GPLv3.0 Java CFML runtime.   OpenBD has a long legacy, first created in 2000, but it is no longer being actively maintained.

### Official Website

The official website for OpenBD is located:

http://openbd.org/

You can download releases from https://github.com/OpenBD/openbd-core/releases

### Running OpenBD

You can run OpenBD quickly without all the hassle of setting and configuring a JEE server.   Use the [JettyDesktop launcher](https://github.com/aw20/jettydesktop) - instructions on use located at the [JettyDesktop Wiki](https://github.com/aw20/jettydesktop/wiki).   This will run the OpenBD WAR file easily and quickly.

### Building OpenBD

You will require the following tools to be able to build OpenBD from source:

* Java Developers Kit Virtual Machine 8
* Apache Ant (http://ant.apache.org/)

Optional, OpenBD source drop includes an Eclipse project to enable building and debugging under the Eclipse IDE (http://www.eclipse.org/).   All files required to build the core OpenBD JAR/WAR is contained within this GitHub project.

All the necessary scripts and resources are in the ./build/ folder.   The build.xml will build the core WAR file, the OpenBlueDragon.jar file as well as popping in the manual if you desire.

* ant compile
* ant war
* ant war-with-manual

If you are just updating the core OpenBD JAR file in an already existing installation then 'ant compile' will create the file: ./build/targets/OpenBlueDragon.jar.   You can then drop this file over the top of an existing installation if you are say upgrading a minor update.  The ReleaseNotes.txt will indicate if a full /WEB-INF/lib/ folder update is required.

'ant war' will build a standard JEE WAR file that can be dropped into any JEE application server, such as Jetty, Tomcat or JBoss.  'ant war-with-manual' will build the standard JEE war but will also pull in the latest 'openbd-manual' (https://github.com/OpenBD/openbd-manual) and add that as part of the distribution.

All output from the ant scripts will be in the /build/targets/

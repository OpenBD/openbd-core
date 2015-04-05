OpenBD is the worlds first truly open source and free GPL Java CFML runtime.

### Official Website

http://openbd.org/

OpenBD and CFML Manual:  http://openbd.org/manual/

### Running OpenBD

You can run OpenBD quickly without all the hassle of setting and configuring a JEE server.   Use the [JettyDesktop launcher](https://github.com/aw20/jettydesktop) - instructions on use located at the [JettyDesktop Wiki](https://github.com/aw20/jettydesktop/wiki).   This will run the OpenBD WAR file easily and quickly.

### Official Support

OpenBD is owned and maintained by aw2.0 Ltd.  Official support subscriptions can be purchased from aw2.0 Ltd. 

For more details visit: http://aw20.co.uk/openbd/

### Community Mailing List

An active Google Groups list can be found: https://groups.google.com/forum/#!forum/openbd

### Building OpenBD

You will require the following to be able to build OpenBD from source:

* Java Developers Kit Virtual Machine 8
* Apache Ant (http://ant.apache.org/)

Optional, OpenBD source drop includes an Eclipse project to enable building and debugging under the Eclipse IDE (http://www.eclipse.org/). 

All the necessary scripts and resources are in the ./build/ folder.   The build.xml will build the core WAR file, the OpenBlueDragon.jar file as well as popping in the manual if you desire.

* ant compile
* ant war
* ant war-with-manual

If you are just updating the core OpenBD JAR file in an already existing installation then 'ant compile' will create the file: ./build/targets/OpenBlueDragon.jar.   You can then drop this file over the top of an existing installation if you are say upgrading a minor update.  The ReleaseNotes.txt will indicate if a full /WEB-INF/lib/ folder update is required.

'ant war' will build a standard JEE WAR file that can be dropped into any JEE application server, such as Jetty, Tomcat or JBoss.  'ant war-with-manual' will build the standard JEE war but will also pull in the latest 'openbd-manual' (https://github.com/OpenBD/openbd-manual) and add that as part of the distribution.

All output from the ant scripts will be in the /build/targets/

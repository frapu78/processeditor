Homepage
========
The homepage of the Process Editor Project is hosted here: http://frapu.de/code/processeditor/index.html

ProcessEditor Core Packages
===========================
* com.inubit.research: Complex Process Workbench (Swing and WebModeler)
* com.frapu.net: Simple Java Canvas for creating ProcessModels incl. BPMN and Petri nets

Java Libaries:
==============
Please download separately and copy to lib folder
* log4j (https://logging.apache.org/log4j/1.2/download.html)
* javax.mail (http://www.oracle.com/technetwork/java/javamail/index.html)
* org.apache.commons.collections (http://commons.apache.org/proper/commons-collections/download_collections.cgi)
* VectorGraphics2D (http://trac.erichseifert.de/vectorgraphics2d/)

WebModeler requires ExtJS: Please download separately and copy ExtJs to www/js/ext (unzipped)
* ExtJS 4 (for Web Modeler, https://ext4all.com/post/how-to-download-extjs-4-gpl.html)

Manual Building (Idea, Netbeans, etc.)
======================================
* Create a new Java 1.7 source project with the required libs as dependencies
* Add the "src" folder as source
* Add the "resources", "pics", "www" folder as resources (Idea) or source (Netbeans)
* Select "com.inubit.research.gui.Workbench" as main class for Workbench
* Select "com.inubit.research.server.ProcessEditorServer" as main class for Server

Ant Building
============
Use the build.xml with the following targets
* "clean-build-workbench": Builds a jar with a manifest file for the Workbench
* "clean-build-server": Builds a jar with a manifest file for the Server (incl. the "www" resources)

Manual Startup
==============
* Workbench: java -cp 'processeditor.jar:lib/*' -Xmx1024m com.inubit.research.gui.Workbench
* Server: java -cp 'processeditor.jar:lib/*' -Xmx1024m com.inubit.research.server.ProcesseditorServer

Automatic Startup (Server, Linux)
=================================
* SystemV (/etc/init.d): Set a symlink to wm-server.sh and configure the user and path in the file. Use 'chkconfig wm-server.sh on' to enable the service.
* systemd (/etc/systemd/system): Copy the wm-server.service file and configure the user and path. Use 'systemctl enable wm-server' to enable the service.

Certificates
============
* Configure certificates as described in www/config/ssl_config.properties.template
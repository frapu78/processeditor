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
* log4j
* javax.mail
* org.apache.commons.collections

WebModeler requires ExtJS: Please download separately and copy ExtJs to www/js/ext (unzipped)
* ExtJS 4 (for Web Modeler)

Manuel Building (Idea, Netbeans, etc.)
======================================
* Create a new Java 1.7 source project and add the "src" folder as source
* Add the "resources", "pics", "www" folder as resources (Idea) or source (Netbeans)
* Select "com.inubit.research.gui.Workbench" as main class for Workbench
* Select "com.inubit.research.server.ProcessEditorServer" as main class for Server

Ant Building
============
Use the build.xml with the following targets
* "clean-build-workbench": Builds a jar with a manifest file for the Workbench
* "clean-build-server": Builds a jar with a manifest file for the Server (incl. the "www" resources)

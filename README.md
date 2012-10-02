PerfLogViewer
=============

A graphic PerfMon log file viewer

1. PerfLogViewer depends on SWT, which is platform-dependant. Modify dependancy artifactId in the pom file as the following if your OS is other than 64bit Windows.

org.eclipse.swt.win32.win32.x86
org.eclipse.swt.win32.win32.x86_64
org.eclipse.swt.cocoa.macosx
org.eclipse.swt.cocoa.macosx.x86_64

Refer to http://stackoverflow.com/questions/292548/how-do-you-build-an-swt-application-with-maven for more details. 

2. Run the following Maven command to execute the program
mvn exec:java -Dexec.mainClass="cn.alan.perflogviewer.viewer.MainWindow"
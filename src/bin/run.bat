@echo off

set JARS=

for %%j in (.\lib\*.jar) do call :add_jar %%j
  
start javaw -Xmx1024m -cp %JARS% cn.alan.perflogviewer.viewer.MainWindow
 
exit /b   
  
:add_jar   
set JARS=%JARS%;%1
exit /b
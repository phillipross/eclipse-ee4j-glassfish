REM These jar files must be present!
set STD_JARS=jmxri.jar;jmxremote.jar;javax77.jar
set CP=.;amx-client.jar;%STD_JARS%

java -cp %CP% com.sun.appserv.management.sample.SampleMain .\SampleMain.properties


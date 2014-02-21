ceylon-jboss-loader
===================

Module Loader for JBoss and Wildfly that allows Ceylon modules in web applications

Sample Web Application
----------------------
See [`https://github.com/dgwave/ceylon-jboss-loader/tree/master/src/test/resources/webapp`](https://github.com/dgwave/ceylon-jboss-loader/tree/master/src/test/resources/webapp)
- The Ceylon module containing your Ceylon servlets needs to be defined in [`https://github.com/dgwave/ceylon-jboss-loader/blob/master/src/test/resources/webapp/WEB-INF/jboss-deployment-structure.xml`](https://github.com/dgwave/ceylon-jboss-loader/blob/master/src/test/resources/webapp/WEB-INF/jboss-deployment-structure.xml) as `<resource-root path="WEB-INF/lib/hello.world-1.0.0.car"/>` or any other path within your WAR.
- For now, immediate dependencies of your srvlet Ceylon module need to be defined similar to `<module name="ceylon.html" slot="1.0.0"/>`.  Transitive dependencies are handled automatically. With an upcoming Wildfly subssystem that we are developing, this will not be necessary.

Adding Ceylon Add-on Layer to Wildfly 8.0
-----------------------------------------
1. Create Ceylon add on layer by creating directory `wildfly-8.0.0.Final/modules/system/add-ons/ceylon`
2. Copy the contents of your deployment-ready Ceylon distribution repo directory into this Ceylon add-on layer directory
3. Copy the contents of your deployment-ready Ceylon SDK and any other Ceylon or Ceylon-ized Java modules into this same  Ceylon add-on layer directory
4. Build the Ceylon loader (this project) with `mvn clean package`
5. Copy `ceylon-loader-0.5.jar` from the `target` directory into the Ceylon add-on layer under the `com/dgwave/car/loader/main` sub-directory
6. Copy `src/main/resources/module/main/module.xml` into the same directory as (5) above

Enabling in Wildfly 8.0 Server embedded in Eclipse IDE
------------------------------------------------------
1. Open Wildfly Runtime Server configuration (Needs JBoss Studio or Wildfly server plugin)
2. Click on `Open Launch Configuration` and select the `Classpath` tab
3. Under `User Entries`, find `ceylon-loader-0.5.jar` where you copied it into the Ceylon add-on layer
4. Select the `Arguments` tab and add this at the end of the `VM Arguments` box: ` -Dboot.module.loader=com.dgwave.car.loader.CarModuleLoader`

Enabling Wildfly 8.0 Server in Windows
--------------------------------------
- For Windows: Add this at the very end of your `standalone.conf.bat` or `domain.conf.bat`:
```
set "JAVA_OPTS= -classpath %JBOSS_HOME%\jboss-modules.jar;%JBOSS_HOME%\modules\system\add-ons\ceylon\com\dgwave\car\loader\ceylon-loader-0.5.jar -Dboot.module.loader=com.dgwave.car.loader.CarModuleLoader %JAVA_OPTS%"
```
- For Windows: Replace this line in `standalone.bat` or `domain.bat`.  This is supported as JBoss Studio works with this mechanism:
```
-jar "%JBOSS_HOME%\jboss-modules.jar" ^
```
with 
```
org.jboss.modules.Main ^
```
 
Enabling JBoss/Wildfly Server in Unix/Linux
-------------------------------------------
Very similar to above, but still to be tested. TBD

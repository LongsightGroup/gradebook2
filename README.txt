README
---------------------------------------------------------------------------------

This file provides some basic information for developers to set up their
development environment to make modifications to the Gradebook 2 project. In
order to simply build and deploy the project in Sakai, refer to the
"INSTALL.txt" file. 

Some of the complexity here derives from the fact that we have two distinct
"setups" involved -- one for Eclipse and one for Maven. This should be
simplified in the future. If you have completed the sets to build and deploy
to Sakai, as indicated above, then you have your environment correctly setup
for Maven. 

The following steps will set up your Eclipse for "Hosted Mode" development.
For more information about Hosted Mode and Web Mode, you may want to read through the
Google Web Toolkit developer guide (http://code.google.com/webtoolkit/).


OS SETUP - Environment variables
-----------------------------------------------------------------------------------
Note: Unless you want to make use of the GradebookApplication-shell and
GradebookApplication-compile scripts (currently missing Sakai dependencies --
this will be resolved by modifying GradebookApplication-classpath to consult
the Eclipse .classpath file, in the near future) then you do not need to set
up any environment variables for pure Hosted Mode. However, you will need
GWT_EXTERNAL_BROWSER to be set if you wish to run in Web Mode.

GWT_EXTERNAL_BROWSER = Your browser binary if you want to use web mode 
GWT_HOME = The directory where you've unpacked the GWT distribution -- currently this should be 1.5.3, e.g. gwt-linux-1.5.3 -- should contain gwt-user.jar and gwt-dev-[platform].jar
GWT_LIB  = The directory where you've placed all the other GWT library jars you'll need, GWT-SL, GWT-DND, etc. -- for convenience, these are all available under ./lib for the moment


ECLIPSE SETUP - Classpath Variables
-----------------------------------------------------------------------------------
GWT_HOME = Same as above, but needs to be a classpath variable in Eclipse, too
GWT_LIB  = Same as above
M2_REPO  = Like normal Sakai, we use the Maven repository for other dependencies

Copy GradebookApplication.launch.tmpl to GradebookApplication.launch -- this
allows you to make modifications to your JRE, etc. without committing them
back to Subversion.


RUNNING HOSTED MODE
-----------------------------------------------------------------------------------

If you're in a 64 bit OS, you'll need to use a 32 bit Java for the Run
command. Navigate (in Eclipse) to Run Configurations/Java
Application/GradebookApplication (or GradebookApplication MAC, if you're on
Mac). Select the JRE tab and choose an Alternate JRE. Point this toward your
x32 JDK. Apply Changes. 

You should now be able to click "Run" and the Google browser will launch. 


CONFIGURATION FILES:
-----------------------------------------------------------------------------------
This is just a quick summary of interesting/important files.

client/src/java/org.sakaiproject.gradebook.gwt/GradebookApplication.gwt.xml -- This is where you tell GWT where to find the RPC servlet
client/src/java/org.sakaiproject.gradebook.gwt/GradebookApplicationFirefox.gwt.xml -- Used when the firefox.patch is applied to build (more rapidly) a version of the app that only runs on Firefox
client/src/main/webapp/WEB-INF/applicationContext.xml -- The spring bean setup, which you only really need if you want to deploy inside Sakai
client/src/main/webapp/WEB-INF/sakai.gradebook.gwt.rpc-servlet.xml -- The GWT-SL setup, which links your GradebookToolFacadeImpl Spring bean to a URL via Spring MVC 
client/src/main/webapp/WEB-INF/web.xml -- This one is what the maven build uses. For Hosted mode, GWT has it's own generated web.xml under tomcat/conf
client/pom.xml -- This is the file that you need to nest Gradebook-GWT inside of Sakai
client/pom-standalone.xml -- This should be switched out for pom.xml if you want to use Maven to build Gradebook-GWT as a standalone servlet
pom.xml -- This is really not necessary, except in combination with pom-standalone.xml -- if you're building using maven for Sakai deployment, just run mvn clean install sakai:deploy at the client/ directory level


GWT-RPC SERVLET / TOOL FACADE CLASSES:
client/src/java/org.sakaiproject.gradebook.gwt.sakai.GradebookToolFacadeImpl.java		-- The actual Sakai tool facade for the Gradebook data model/api
client/src/java/org.sakaiproject.gradebook.gwt.sakai.mock.GradebookToolFacadeMockImpl.java	-- A mock version of the above that simulates the Sakai services necessary (this is the one we use for Hosted Mode)


# userlessbuild

This tool is used to add jobs to Headless Designer 9.0.1. Please, have a look at designer wiki for background information.

	http://www-10.lotus.com/ldd/ddwiki.nsf/dx/Headless_Designer_Wiki

This tool can be installed into domino Designer using standard procedure. It also provides a set of ant tasks that allows you to automate the generation of your NSF files and update sites. 

## Download update site and jar file ##

A ready to install version of the update site is available at :

<a href="https://www.dropbox.com/sh/fh3ambprjq8cfwt/AADE_r8meYTwky4jdqqe00zGa/fr.asi.designer.userlessbuild.site.zip?dl=0">Update Site for Designer plugins</a>

And a ready to use version of the jar that contains the ant tasks is available at :

<a href="https://www.dropbox.com/sh/fh3ambprjq8cfwt/AAAfD5XKZXRPvZ7ExHNDit_Da/userlessbuild-ant-tasks.jar?dl=0">Ant Tasks for Headless Designer</a>

## Compile it yourself ##

- Import projects into Designer (right click into package explorer / Import / Existing project into Workspace)
- You will have to add dependencies on ant jars and on notes.jar to compile the project that contains the ant tasks. But event if it does not compile, you will be able to add the commands into your designer.
- Compile update site:
	- Open fr.asi.designer.userlessbuild.site/site.xml file 
	- Click on "Build All". This will work even if the ant tasks project contains compilation errors.
- Use Domino Designer File/Application/Install (check preferences if menu entry is not present)
	- Add new feature
	- Add Folder Update Site that points to fr.asi.designer.userlessbuild.site
- Accept licenses, and restart Designer

# Additional Designer Commands #

## Compile update site ##

This job can be used to compile an already imported update site. This is the exact equivalent of opening the site.xml file, and clicking "Build All".

Example command file :

	config,true,true
	fr.asi.designer.userlessbuild.HDBuildSiteJob,<name of the project to build>
	exit

## Wait for automatic build to finish ##

This job can be used to wait for build jobs to stop. Very usefull after importer a bunch of java projects, and you want that eclipse only exits when compilation is over.

Example command file :

	config,true,true
	com.ibm.designer.domino.tools.userlessbuild.jobs.ImportOnDiskProjectJob,<path to .project file>
	fr.asi.designer.userlessbuild.WaitForBuildJob,<name of the project to build>
	exit

## Check for errors ##

This job will check if there are some errors into a given project. It will work on java projects (your Library plugins for example), and on Domino projects.

Note that the job will simply fail with an ERROR state. So will have to define "config,true,true" at the beginning of your command file to make it work.

Example command file :

	config,true,true
	importandbuild,d:/Sources/mydatabase-ondisk/.project,mydatabase.nsf
	wait,mydatabase.nsf,20
	fr.asi.designer.userlessbuild.CheckErrorJob,mydatabase.nsf
	exit

This command file will fail with errors if mydatabase.nsf contains errors.

# Additionnal Ant tasks #

## Running ant tasks ##

First of all, you will have to install ant. This is pretty simple.

Next, you will have to ask ant to run build.xml files using the local Notes Client JVM. For this, make the JAVA_HOME environment variable point to

	${NOTES_ROOT}/jvm  

Where NOTES_ROOT is where you installed your notes client.

## Declaring the tasks in a build.xml file ##

Put the jar file in a "lib" folder, next to the build.xml file. 

To include the tasks, just use a standard taskdef tag :

	<project name="test">
		...
		<path id="project.class.path">
			<fileset dir="lib" includes="*.jar" />
		</path>
		<taskdef
				resource="userlessbuild-ant-tasks.properties"
				classpathref="project.class.path"/>
		...
	</project>

## Tasks to manipulate source code ##

### runDesignerCommands ###

This command will :

- Launch Domino Designer in headless mode
- Make it execute the commands you put inside the tag
- It will then loop until Designer stop
- While looping, it will check for the log files, and send their content to the console.

If an error is detected inside the log file, the task will fail.

	<runDesignerCommands designerPath="C:\Notes\">
		config,true,true
		exit
	</runDesignerCommands>

### waitForDesigner ###

Will wait for designer to shutdown. This task doesn't need any parameters.

	<waitForDesigner/>

### setManifestVersion ###

Will update the version inside a MANIFEST.MF file

	<setManifestVersion version="4.6.0" manifestFile="${basedir}/fr.asi.test/META-INF/MANIFEST.MF"/>

### setFeatureVersion ###

Will update the version defined in a feature.xml file

	<setFeatureVersion version="4.6.0" featureXmlFile="${basedir}/fr.asi.test.feature/feature.xml"/>

### setUpdateSiteVersion ###

Will update the features versions inside a site.xml file.

	<setUpdateSiteVersion version="4.6.0" siteXmlFile="${basedir}/fr.asi.test.site/site.xml"/>

### setOnDiskTitle ###

This task will update a ondisk project (mirror of the design of an NSF file) to change the database title.

	<setOnDiskTitle onDiskPath="${basedir}/mydatabase-ondisk" title="New Title"/>

### setOnDiskTemplate ###

This task will change the name of the template that a given database is declaring. It will make the needed changes inside an ondisk project (mirror of the design of an NSF file).

	<setOnDiskTemplate onDiskPath="${basedir}/mydatabase-ondisk" masterTemplateName="mytmpl"/>

## Tasks to manipulate Domino Servers ##

These tasks are using the standard notes apis. They will open sessions to the Notes server using the local Notes client. For this, you will have to give them the password of the local ID file.

### DatabaseSet ###

Some tasks supports a nested <databaseSet> tag which allows the task to run on multiple databases. On these tasks, you can also define the following properties :

- server: the server to find the database
- password: Password of the local ID file to access to the database
- database: A database to run on. This is a shortcut when processing only one database.

Launch a task on a single database :
 
	<atask server="SERVER/ASI" password="mypassword" database="db.nsf"/>

Same a previous task :

	<atask server="SERVER/ASI" password="mypassword">
		<databaseSet database="mydb.nsf"/>
	</atask>

Run on multiple databases :

	<atask server="SERVER/ASI" password="mypassword">
		<databaseSet database="mydb1.nsf"/>
		<databaseSet database="mydb2.nsf"/>
	</atask>
 
Run on all databases that inherits from template "tmpl" :

	<atask server="SERVER/ASI" password="mypassword">
		<databaseSet template="tmpl"/>
	</atask>

Run on db1, db2 and on all database that inherits from tmpl :
	
	<atask server="SERVER/ASI" password="mypassword" database="db1.nsf">
		<databaseSet database="db2.nsf"/>
		<databaseSet template="tmpl"/>
	</atask>

### sendConsole ###

This task allows you to send a console command, and wait for it to finish, or wait for it to start.

This task will send a "tell http quit", and will wait until the result of the "show task" command no longer contains the "HTTP Server" expression.

	<sendConsole 
		password="mypassword" 
		server="SERVER/ASI" 
		command="tell http quit" 
		taskRunningMessage="HTTP Server"/>

This task will launch the designer task, and wait for it to shutdown. It will loop while it found the "Designer" expression into the result of a "show task" command.

	<sendConsole
		password="mypassword"
		server="SERVER/ASI"
		command="load design -f names.nsf"
		taskRunningMessage="Designer"/>

This task will load the http task, and wait for it to start. It will loop while it does NOT found in the result of a "show task" command the expression given in the taskStartedMessage property.

	<sendConsole 
		password="mypassword"
		server="SERVER/ASI" 
		command="load http" 
		taskStartedMessage="HTTP Server[ ]*Listen for connect requests on TCP Port:"/>

Note that taskStartedMessage and taskRunningMessage are regular expressions.


### httpStop ###

Will stop the http task. Password is the password of your local ID file.

	<httpStop server="SERVER/ASI" password="mypassword"/>

### httpStart ###

Will start the http task. Password is the password of your local ID file.

	<httpStart server="SERVER/ASI" password="mypassword"/>

### databaseDelete ###

Will remove a database

	<databaseDelete server="SERVER/ASI" database="mydatabase.nsf" password="mypassword"/>

### databaseCopy ###

Will copy a database.

	<databaseCopy srcServer="" srcDatabase="mydb.ntf" destServer="SERVER/ASI" destDatabase="mydb.ntf" password="mypassword" templateCheck="mytmpl"/>

The "templatecheck" parameter will make the task check that the copied database is declaring the given template name (after copy). This will fail if the server already contains a database that declares the same template name. Yes, I love Domino... 

### signWithServerId ###

Will sign the given database with the server ID. This task will create an administration request, and wait for adminp to process it.

	<signWithServerId server="SERVER/ASI" database="mydatabase.nsf" password="mypassword"/>

### clearUpdateSiteDb ###

Deprecated. Use clearDb instead

This task will clear an update site database.

	<clearUpdateSiteDb server="SERVER/ASI" database="updateSite.nsf" password="mypassword"/>

### clearDb ###

This task will remove documents from a database.

	<clearDb server="SERVER/ASI" database="updateSite.nsf" password="mypassword" formula="Form = 'MyForm'/>

If no formula is specified, then all documents will be removed from the database.

### runAgent ###

This task will run an agent. It will make it run on a context document, on which you will be able to add fields using nested contextDocField tags.

	<runAgent server="SERVER/ASI" database="db.nsf" agent="myagent" password="mypassword">
		<contextDocField name="MyField" value="my value"/>
		<contextDocField name="MyField2" value="my other value"/>
	</runAgent>

### refreshDesign ###

This task will refresh the design of the given database. The refresh is done via the domino "designer" task. The task will end when it will detect that the domino "designer" task ends.

	<refreshDesign server="SERVER/ASI" database="mydb.nsf" password="mypassword"/>

This task also support nested <databaseSet> tags.

	<refreshDesign server="SERVER/ASI" password="mypassword">
		<databaseSet template="tmpl"/>
	</refreshDesign>

### setOnBehalfOf ###

This task will make an agent run "on behalf of" a given user.

	<setOnBehalfOf server="SERVER/ASI" database="mydb.nsf" agent="myagent" onBehalfOf="CN=Lionel HERVIER/O=ASI" password="mypassword"/>

This task also support nested <databaseSet> tags.

	<setOnBehalfOf server="SERVER/ASI" agent="myagent" onBehalfOf="CN=Lionel HERVIER/O=ASI" password="mypassword">
		<databaseSet template="tmpl"/>
	</setOnBehalfOf>

### enableAgent ###

This task will allows you to enable an agent

	<enableAgent server="SERVER/ASI" database="mydb.nsf" agent="myagent" serverToRun="SERVER2/ASI" password="mypassword"/>

The "serverToRun" is the name of the server the agent will be activated on.

This task also support nested <databaseSet> tags.

	<enableAgent server="SERVER/ASI" agent="myagent" serverToRun="SERVER2/ASI" password="mypassword">
		<databaseSet template="tmpl"/>
	</enableAgent>

### dxlExport ###

This task export a set of documents (selection based on a formula) to a dxl file.
The dxl file will be cleaned so that all information relative to the local database or local documents are removed: database replica id, document unid, last update date, last update author, etc...

	<dxlExport 
		password="mypassword" 
		server="SERVER/ASI" 
		database="mydb.nsf" 
		formula="Form = 'MyForm'" 
		toFile="export.dxl"/>

### dxlImport ###

This task will import documents stored in a dxl file.

	<dxlImport
		password="mypassword"
		server="SERVER/ASI"
		database="mydb.nsf"
		fromFile="file.dxl"/>

dxlImport can also work on a databaseSet. This task will import documents from the dxl file into every databases that rely on the template "mytemplate".

	<dxlImport password="mypassword" server="SERVER/ASI" fromFile="file.dxl">
		<databaseSet template="mytemplate"/>
	</dxlImport>

### fieldExport ###

This task will only export a set of fields from a document

	<fieldExport
		password="mypassword"
		server="SERVER/ASI"
		database="mydb.nsf"
		formula="Form = 'param'"
		fields="Field1,Field2,Field3"
		toFile="fields.xml"/>

This will create a file named fields.xml that contains a subset of the DXL extract. All field types are supported (including rich text).

	<?xml version="1.0" encoding="UTF-8"?>
	<document xmlns="http://www.lotus.com/dxl">
  		<item name="Field1">
    		<text>value1</text>
  		</item>
		<item name="Field2">
			<text>value1</text>
			<text>value2</text>
		</item>
		<item name="Field3">
			<datetime>20161123T094516,00+01</datetime>
		</item>
	</document>

Note that the task will fail if the formula selects more than one document.

### fieldImport ###

This task will import a set of fields in a set of documents. This task can take databaseSets.

	<fieldImport
			password="mypassword"
			server="SERVER/ASI"
			formula="Form = 'MyForm'"
			fromFile="fields.xml">
		<databaseSet template="mytemplate"/>
	</fieldImport>

Every documents that are using the "MyForm" form in every databases that a using the "mytemplate" template will be updated with the fields defined in the fields.xml file.

If a field already exists in a document, it will be overwritten (including its type). If it does not exists, it will be created.

### clearProhibitDesignRefresh ###

This task allows you to remove the "Prohibit design refresh" flag on a set design elements.

	<clearProhibitDesignRefresh
			password="mypassword"
			server="SERVER/ASI"
			select="VIEWS">
		<databaseSet template="mytemplate"/>
	</clearProhibitDesignRefresh>

This task will remove the flag from all the views that are present in any database that relies on the "mytemplate" template.

Possible values for the select property are :

	+------------------------+---------------------------+
	| ALL                    | All design elements       |
	| ACTIONS                | Actions                   |
	| AGENTS                 | Agents (LS and Java)      |
	| APPLETS                | Applets                   |
	| DATABASE_SCRIPTS       | Database scripts          |
	| COLUMNS                | Shared columns            |
	| DATA_CONNECTIONS       | Data connections          |
	| FILE_RESOURCE          | Files                     |
	| HIDDEN_FILE            | Hidden files (for xpages) |
	| CUSTOM_CONTROLS        | Custom controls           |
	| THEMES                 | Themes                    |
	| XPAGES                 | XPages                    |
	| FOLDERS                | Folders                   |
	| FORMS                  | Forms                     |
	| FRAMESETS              | Framesets                 |
	| NAVIGATORS             | Navigators                |
	| OUTLINES               | Outlines                  |
	| PAGES                  | Pages                     |
	| PROFILES               | Profiles documents        |
	| SCRIPT_LIBRARIES       | Script libraries          |
	| WEB_SERVICE_CONSUMERS  | Web service consumers     |
	| WEB_SERVICE_PROVIDERS  | Web service providers     |
	| SHARED_FIELDS          | Shared fields             |
	| SUBFORMS               | Subforms                  |
	| VIEWS                  | Views                     |
	| WIRING_PROPERTIES      | Wiring properties         |
	| COMPOSITE_APPLICATIONS | Composite applications    |
	| IMAGES                 | Image resources           |
	| STYLESHEETS            | Stylesheets               |
	| DB2_ACCESS_VIEWS       | DB2 access views          |
	| ICON                   | Icon document             |
	+------------------------+---------------------------+

### clearInheritTemplateFrom ###

This task will clear the name of the template that a design element depends on.

	<clearInheritTemplateFrom
			password="mypassword"
			server="SERVER/ASI"
			select="VIEWS">
		<databaseSet template="mytemplate"/>
	</clearInheritTemplateFrom>

This taks will remove the name of the name of template that every view of every databases that depends on the "mytemplate" depends on.

For the "select" property, you can use the same list as the clearProhibitDesignRefresh task.

### checkXPageCompiled ###

This task will check that the given databases contains only XPages that have been compiled.

	<checkXPagesCompiled
			password="mypassword"
			server="SERVER/ASI"
		<databaseSet template="mytemplate"/>
	</checkXPagesCompiled>
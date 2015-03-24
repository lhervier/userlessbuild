# userlessbuild

This tool is used to add jobs to Headless Designer 9.0.1. Please, have a look at designer wiki for background information.

	http://www-10.lotus.com/ldd/ddwiki.nsf/dx/Headless_Designer_Wiki

This tool can be installed into domino Designer using standard procedure :

- Import projects into Designer (right click into package explorer / Import / Existing project into Workspace)
- Compile update site:
	- Open fr.asi.designer.userlessbuild.site/site.xml file 
	- Click on "Build All"
- Use Domino Designer File/Application/Install (check preferences if menu entry is not present)
	- Add new feature
	- Add Folder Update Site that points to fr.asi.designer.userlessbuild.site
- Accept licenses, and restart Designer

# Additional jobs #

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
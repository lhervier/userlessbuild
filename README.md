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

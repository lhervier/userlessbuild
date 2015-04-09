package fr.asi.designer.anttasks.userlessbuild;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import fr.asi.designer.anttasks.util.Utils;

/**
 * Ant task to change the features versions onto a site.xml file
 * @author Lionel HERVIER
 */
public class SetUpdateSiteVersion extends Task {

	/**
	 * Path to the site.xml file
	 */
	private String siteXmlFile;
	
	/**
	 * Version to set for each feature
	 */
	private String version;
	
	/**
	 * @param siteXmlFile the siteXmlFile to set
	 */
	public void setSiteXmlFile(String siteXmlFile) {
		this.siteXmlFile = siteXmlFile;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Execution
	 */
	public void execute() {
		this.log("Setting features version to " + this.version + " in site.xml file : " + this.siteXmlFile, Project.MSG_INFO);
		try {
			File f = new File(this.getProject().getProperty("basedir") + "/" + this.siteXmlFile);
			String content = Utils.readFile(f);
			
			String site = "";
			boolean inSite = false;
			Reader reader = new StringReader(content);
			BufferedReader breader = new BufferedReader(reader);
			String line = breader.readLine();
			while( line != null ) {
				if( !inSite && line.indexOf("<site") != -1 )
					inSite = true;
				
				if( inSite ) {
					int pos = line.indexOf("version=\"");
					if( pos != -1 ) {
						int pos2 = line.indexOf('"', pos + 9);
						if( pos2 == -1 )
							throw new RuntimeException("Incorrect site.xml file");
						line = line.substring(0, pos) + "version=\"" + version + line.substring(pos2);
					}
					
					pos = line.indexOf("url=\"");
					if( pos != -1 ) {
						int pos2 = line.indexOf('_', pos + 1);
						if( pos2 == -1 )
							throw new RuntimeException("Incorrect site.xml file");
						int pos3 = line.indexOf(".jar\"", pos2 + 1);
						if( pos3 == -1 )
							throw new RuntimeException("Incorrect site.xml file");
						line = line.substring(0, pos2) + "_" + version + line.substring(pos3);
					}
				}
				
				site += line + "\r\n";
				line = breader.readLine();
			}
			breader.close();
			reader.close();
			
			Utils.createFile(f, site);
		} catch (IOException e) {
			this.log(e, Project.MSG_ERR);
			throw new RuntimeException(e);
		} finally {
			
		}
	}
}

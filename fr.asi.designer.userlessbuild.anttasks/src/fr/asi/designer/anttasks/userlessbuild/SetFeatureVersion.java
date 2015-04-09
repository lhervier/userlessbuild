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
 * Task to set the version number in a feature.xml file
 * @author Lionel HERVIER
 */
public class SetFeatureVersion extends Task {

	/**
	 * The file to update
	 */
	private String featureXmlFile;
	
	/**
	 * The version to set
	 */
	private String version;
	
	/**
	 * @param featureXmlFile the featureXmlFile to set
	 */
	public void setFeatureXmlFile(String featureXmlFile) {
		this.featureXmlFile = featureXmlFile;
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
		try {
			this.log("Setting version " + this.version + " in feature.xml file : " + this.featureXmlFile, Project.MSG_INFO);
			File f = new File(this.getProject().getProperty("basedir") + "/" + this.featureXmlFile);
			
			String content = Utils.readFile(f);
				
			boolean inFeature = false;
			boolean updated = false;
			String manifest = "";
			Reader reader = new StringReader(content);
			BufferedReader breader = new BufferedReader(reader);
			String line = breader.readLine();
			while( line != null ) {
				if( !inFeature && line.indexOf("<feature") != -1 )
					inFeature = true;
				
				int pos = line.indexOf("version=\"");
				if( inFeature && !updated && pos != -1 ) {
					int pos2 = line.indexOf('"', pos + 9);
					if( pos2 == -1 )
						throw new RuntimeException("Fichier feature.xml incorrect");
					line = line.substring(0, pos) + "version=\"" + this.version + line.substring(pos2);
					
					updated = true;
				}
				manifest += line + "\r\n";
				line = breader.readLine();
			}
			breader.close();
			reader.close();
			
			Utils.createFile(f, manifest);
		} catch (IOException e) {
			this.log(e, Project.MSG_ERR);
			throw new RuntimeException(e);
		}
	}
}

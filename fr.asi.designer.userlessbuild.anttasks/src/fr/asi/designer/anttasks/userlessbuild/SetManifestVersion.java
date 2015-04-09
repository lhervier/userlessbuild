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
 * Task to update version in a MANIFEST.MF file
 * @author Lionel HERVIER
 */
public class SetManifestVersion extends Task {
	
	/**
	 * The version to set
	 */
	private String version;
	
	/**
	 * The manifest file to update
	 */
	private String manifestFile;

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @param manifestFile the manifestFile to set
	 */
	public void setManifestFile(String manifestFile) {
		this.manifestFile = manifestFile;
	}
	
	/**
	 * Execution
	 */
	public void execute() {
		try {
			this.log("Setting version " + this.version + " in manifest file: " + this.manifestFile, Project.MSG_INFO);
			
			File f = new File(this.getProject().getProperty("basedir") + "/" + this.manifestFile);
			
			String content = Utils.readFile(f);
			
			String manifest = "";
			Reader reader = new StringReader(content);
			BufferedReader breader = new BufferedReader(reader);
			String line = breader.readLine();
			while( line != null ) {
				if( line.startsWith("Bundle-Version: ") )
					line = "Bundle-Version: " + this.version;
				manifest += line + "\n";
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

package fr.asi.designer.anttasks.userlessbuild;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import fr.asi.designer.anttasks.util.DxlUtils;

/**
 * Task to change the name of an ondisk database.
 * Note that adding \n#1 or \n#2 can allows us to update the master template name.
 * @author Lionel HERVIER
 */
public class SetOnDiskTitle extends Task {

	/**
	 * Path to the ondisk project
	 */
	private String onDiskPath;
	
	/**
	 * The new title
	 */
	private String title;

	/**
	 * @param onDiskPath the onDiskPath to set
	 */
	public void setOnDiskPath(String onDiskPath) {
		this.onDiskPath = onDiskPath;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Execution
	 */
	public void execute() {
		this.log("Setting database title to '" + this.title + "' in project " + this.onDiskPath, Project.MSG_INFO);
		try {
			File root = new File(this.getProject().getProperty("basedir") + "/" + this.onDiskPath);
			
			DxlUtils.updateTitleField(
					new File(root, "Resources/IconNote"), 
					this.title, 
					null, 
					null
			);
			
			DxlUtils.updateTitleField(
					new File(root, "AppProperties/database.properties"), 
					this.title, 
					null, 
					null
			);
			
			DxlUtils.updateDbHeader(
					new File(root, "AppProperties/database.properties"), 
					this.title, 
					null, 
					null
			);
		} catch (IOException e) {
			this.log(e, Project.MSG_ERR);
			throw new RuntimeException(e);
		}
	}
}

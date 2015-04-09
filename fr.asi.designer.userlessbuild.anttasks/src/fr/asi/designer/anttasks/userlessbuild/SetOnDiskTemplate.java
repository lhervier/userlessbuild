package fr.asi.designer.anttasks.userlessbuild;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import fr.asi.designer.anttasks.util.DxlUtils;

/**
 * Task to set the name of the master template defined by a database.
 * It will update an ondisk project, not a real NSF.
 * @author Lionel HERVIER
 */
public class SetOnDiskTemplate extends Task {

	/**
	 * Path to the ondisk project
	 */
	private String onDiskPath;
	
	/**
	 * The new master template name
	 */
	private String masterTemplateName;
	
	/**
	 * @param onDiskPath the onDiskPath to set
	 */
	public void setOnDiskPath(String onDiskPath) {
		this.onDiskPath = onDiskPath;
	}

	/**
	 * @param masterTemplateName the masterTemplateName to set
	 */
	public void setMasterTemplateName(String masterTemplateName) {
		this.masterTemplateName = masterTemplateName;
	}

	/**
	 * Execution
	 */
	public void execute() {
		this.log("Setting master template name to '" + this.masterTemplateName + "' in project " + this.onDiskPath);
		try {
			File root = new File(this.getProject().getProperty("basedir") + "/" + this.onDiskPath);
			
			DxlUtils.updateTitleField(
					new File(root, "Resources/IconNote"), 
					null, 
					this.masterTemplateName, 
					null
			);
			DxlUtils.updateTitleField(
					new File(root, "AppProperties/database.properties"), 
					null, 
					this.masterTemplateName, 
					null
			);
			
			DxlUtils.updateDbHeader(
					new File(root, "AppProperties/database.properties"),
					null,
					this.masterTemplateName,
					null
			);
		} catch (IOException e) {
			this.log(e, Project.MSG_ERR);
			throw new RuntimeException(e);
		}
	}
}

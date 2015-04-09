package fr.asi.designer.anttasks.domino;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import fr.asi.designer.anttasks.util.DominoUtils;

/**
 * Ant task to copy a domino database
 * @author Lionel HERVIER
 */
public class DatabaseCopy extends Task {

	/**
	 * The source server
	 */
	private String srcServer;
	
	/**
	 * The source database
	 */
	private String srcDatabase;
	
	/**
	 * The destination server
	 */
	private String destServer;
	
	/**
	 * The destination database
	 */
	private String destDatabase;
	
	/**
	 * After copying the database, the task will check if the
	 * destination database defines this template name. 
	 */
	private String templateCheck;

	/**
	 * Password of the local Id file
	 */
	private String password;
	
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @param srcServer the srcServer to set
	 */
	public void setSrcServer(String srcServer) {
		this.srcServer = srcServer;
	}

	/**
	 * @param srcDatabase the srcDatabase to set
	 */
	public void setSrcDatabase(String srcDatabase) {
		this.srcDatabase = srcDatabase;
	}

	/**
	 * @param destServer the destServer to set
	 */
	public void setDestServer(String destServer) {
		this.destServer = destServer;
	}

	/**
	 * @param destDatabase the destDatabase to set
	 */
	public void setDestDatabase(String destDatabase) {
		this.destDatabase = destDatabase;
	}

	/**
	 * @param templateCheck the templateCheck to set
	 */
	public void setTemplateCheck(String templateCheck) {
		this.templateCheck = templateCheck;
	}
	
	/**
	 * Execution
	 */
	public void execute() throws BuildException {
		this.log("Copying database '" + this.srcServer + "!!" + this.srcDatabase + "' to '" + this.destServer + "!!" + this.destDatabase + "'");
		try {
			DominoUtils.deleteDatabase(
					this.destServer, 
					this.destDatabase, 
					this.password
			);
			
			DominoUtils.copyDatabase(
					this.srcServer, 
					this.srcDatabase, 
					this.destServer, 
					this.destDatabase, 
					this.password
			);
			
			if( this.templateCheck != null && this.templateCheck.length() != 0 ) {
				String mt = DominoUtils.getMasterTemplateName(this.destServer, this.destDatabase, this.password);
				if( mt == null )
					throw new BuildException("Unable to copy database to '" + this.srcServer + "!!" + this.srcDatabase + "'. Another template is already declared as '" + this.templateCheck + "'", this.getLocation());
			}
		} catch (InterruptedException e) {
			throw new BuildException(e, this.getLocation());
		}
	}
}

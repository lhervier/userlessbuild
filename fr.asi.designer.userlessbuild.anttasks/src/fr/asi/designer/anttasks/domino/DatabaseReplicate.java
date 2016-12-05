package fr.asi.designer.anttasks.domino;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import fr.asi.designer.anttasks.util.ConsoleException;
import fr.asi.designer.anttasks.util.DominoUtils;

/**
 * Send a command on a domino server
 * 
 * @author Lionel HERVIER & Philippe ARDIT
 */
public class DatabaseReplicate extends Task {

	/**
	 * The source server
	 */
	private String srcServer;
	
	/**
	 * The source database
	 */
	private String srcDatabase;
	
	/**
	 * The destination server(s separated by semi-columns)
	 */
	private String destServer;
	
	/**
	 * Password of the local Id file
	 */
	private String password;
	
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
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Execution
	 */
	public void execute() throws BuildException {
		try {
			String[] tabServers = this.destServer.split(";");
			for (int i = 0; i < tabServers.length; i++) {
				this.log("Replicating database '" + this.srcServer + "!!" + this.srcDatabase + "' to '" + tabServers[i] + "'");
				DominoUtils.sendConsole(this.srcServer, "Replicate " + tabServers[i] + " " + this.srcDatabase, this.password);
			}
			this.log("Replication command launched... please check manually", Project.MSG_INFO);
		} catch( ConsoleException e) {
			this.log(e, Project.MSG_ERR);
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			this.log(e, Project.MSG_ERR);
			throw new RuntimeException(e);
		}
	}
}

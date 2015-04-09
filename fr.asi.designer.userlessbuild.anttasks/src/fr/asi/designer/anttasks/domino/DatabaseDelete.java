package fr.asi.designer.anttasks.domino;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import fr.asi.designer.anttasks.util.DominoUtils;

/**
 * Ant task to remove a database
 * @author Lionel HERVIER
 */
public class DatabaseDelete extends Task {

	/**
	 * Server where to find the database
	 */
	private String server;
	
	/**
	 * Name of the database to remove
	 */
	private String database;
	
	/**
	 * Password of the local id file
	 */
	private String password;

	/**
	 * @param server the server to set
	 */
	public void setServer(String server) {
		this.server = server;
	}

	/**
	 * @param database the database to set
	 */
	public void setDatabase(String database) {
		this.database = database;
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
	public void execute() {
		this.log("Removing database " + this.server + "!!" + this.database);
		try {
			DominoUtils.deleteDatabase(this.server, this.database, this.password);
		} catch (InterruptedException e) {
			this.log(e, Project.MSG_INFO);
			throw new RuntimeException(e);
		}
	}
}

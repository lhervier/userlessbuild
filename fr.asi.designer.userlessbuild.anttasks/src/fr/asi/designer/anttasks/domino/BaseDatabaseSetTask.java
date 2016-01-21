package fr.asi.designer.anttasks.domino;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import fr.asi.designer.anttasks.util.Utils;

/**
 * Base task for tasks that work with databaseSets
 * @author Lionel HERVIER
 */
public abstract class BaseDatabaseSetTask extends Task {
	
	/**
	 * The server
	 */
	private String server;
	
	/**
	 * The password of the local ID file
	 */
	private String password;

	/**
	 * The database
	 */
	private String database;
	
	/**
	 * The database set
	 */
	private List<DatabaseSet> databases = new ArrayList<DatabaseSet>();
	
	/**
	 * Create an empty databaseSet
	 */
	public DatabaseSet createDatabaseSet() {
		DatabaseSet ret = new DatabaseSet();
		ret.setServer(this.server);
		ret.setPassword(this.password);
		this.databases.add(ret);
		return ret;
	}
	
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
	 * @return the server
	 */
	public String getServer() {
		return server;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Execution on a given database
	 * @param dbPath
	 * @throws BuildException
	 */
	public abstract void execute(String dbPath) throws BuildException;
	
	/**
	 * Execution
	 */
	public void execute() throws BuildException {
		// Extract databases file path
		List<String> dbs = new ArrayList<String>();
		if( !Utils.isEmpty(this.database) )
			dbs.add(this.database);
		for( DatabaseSet s : this.databases )
			dbs.addAll(s.getPaths());
		
		// Run execution on each database
		for( String db : dbs )
			this.execute(db);
	}

}

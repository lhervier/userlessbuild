package fr.asi.designer.anttasks.conditions;

/**
 * Base Notes condition that rely on a server and a database
 * @author Lionel HERVIER
 */
public abstract class BaseServerDatabaseCondition extends BaseNotesCondition {

	/**
	 * The server
	 */
	private String server;
	
	/**
	 * The database
	 */
	private String database;

	/**
	 * @return the server
	 */
	protected String getServer() {
		return server;
	}

	/**
	 * @return the database
	 */
	protected String getDatabase() {
		return database;
	}

	// =======================================================================
	
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
}

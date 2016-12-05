package fr.asi.designer.anttasks.domino.impl;

import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;
import fr.asi.designer.anttasks.domino.BaseNotesTask;
import fr.asi.designer.anttasks.util.Utils;

/**
 * Ant task to remove a database
 * @author Lionel HERVIER
 */
public class DatabaseDelete extends BaseNotesTask {

	/**
	 * Server where to find the database
	 */
	private String server;
	
	/**
	 * Name of the database to remove
	 */
	private String database;
	
	/**
	 * @see fr.asi.designer.anttasks.domino.BaseNotesTask#execute(lotus.domino.Session)
	 */
	@Override
	public void execute(Session session) throws NotesException {
		this.log("Removing database " + this.server + "!!" + this.database);
		Database db = null;
		try {
			db = session.getDatabase(
					this.server, 
					this.database, 
					false
			);
			if( db == null )
				return;
			try {
				db.remove();
			} catch(NotesException e) {
				// May happen...
			}
		} finally {
			Utils.recycleQuietly(db);
		}
	}
	
	// =========================================================================
	
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

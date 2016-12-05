package fr.asi.designer.anttasks.domino.impl;

import lotus.domino.Database;
import lotus.domino.DocumentCollection;
import lotus.domino.NotesException;
import lotus.domino.Session;
import fr.asi.designer.anttasks.domino.BaseNotesTask;
import fr.asi.designer.anttasks.util.Utils;

/**
 * Ant task to clear the content of an update site database
 * @author Lionel HERVIER
 */
public class ClearUpdateSiteDb extends BaseNotesTask {

	/**
	 * The server where to find the update site db
	 */
	private String server;
	
	/**
	 * The database
	 */
	private String database;
	
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
	 * @see fr.asi.designer.anttasks.domino.BaseNotesTask#execute(lotus.domino.Session)
	 */
	@Override
	public void execute(Session session) throws NotesException {
		this.log("Clearing all content in database '" + this.server + "!!" + this.database + "'");
		Database updateSite = null;
		DocumentCollection coll = null;
		try {
			updateSite = this.openDatabase(this.server, this.database);
			coll = updateSite.getAllDocuments();
			coll.removeAll(false);
		} finally {
			Utils.recycleQuietly(coll);
			Utils.recycleQuietly(updateSite);
		}
	}
}

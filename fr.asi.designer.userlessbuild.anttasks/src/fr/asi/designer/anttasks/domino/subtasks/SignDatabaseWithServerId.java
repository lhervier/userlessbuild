package fr.asi.designer.anttasks.domino.subtasks;

import lotus.domino.AdministrationProcess;
import lotus.domino.NotesException;
import lotus.domino.Session;
import fr.asi.designer.anttasks.domino.BaseNotesTask;
import fr.asi.designer.anttasks.util.Utils;

public class SignDatabaseWithServerId extends BaseNotesTask {

	/**
	 * The server to find the database
	 */
	private String server;
	
	/**
	 * The database to sign
	 */
	private String database;
	
	/**
	 * The noteId of the generated admin request
	 */
	private String noteId;
	
	/**
	 * @see fr.asi.designer.anttasks.domino.BaseNotesTask#execute(lotus.domino.Session)
	 */
	@Override
	public void execute(Session session) throws NotesException {
		AdministrationProcess ap = null;
		try {
			ap = session.createAdministrationProcess(server);
			this.noteId = ap.signDatabaseWithServerID(
					this.server, 
					this.database, 
					false
			);
		} finally {
			Utils.recycleQuietly(ap);
		}
	}
	
	/**
	 * @return the noteId
	 */
	public String getNoteId() {
		return noteId;
	}
	
	// ==============================================================================
	
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

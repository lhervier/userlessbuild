package fr.asi.designer.anttasks.domino.impl;

import fr.asi.designer.anttasks.domino.BaseNotesTask;
import lotus.domino.NotesException;
import lotus.domino.Session;

/**
 * Send a command on a domino server
 * 
 * @author Lionel HERVIER & Philippe ARDIT
 */
public class DatabaseReplicate extends BaseNotesTask {

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
	 * @see fr.asi.designer.anttasks.domino.BaseNotesTask#execute(lotus.domino.Session)
	 */
	@Override
	public void execute(Session session) throws NotesException {
		String[] tabServers = this.destServer.split(";");
		for( int i = 0; i < tabServers.length; i++ ) {
			this.log("Replicating database '" + this.srcServer + "!!" + this.srcDatabase + "' to '" + tabServers[i] + "'");
			
			SendConsole task = this.delegate(SendConsole.class);
			task.setServer(this.srcServer);
			task.setCommand("Replicate " + tabServers[i] + " " + this.srcDatabase);
			task.execute();
		}
	}
}

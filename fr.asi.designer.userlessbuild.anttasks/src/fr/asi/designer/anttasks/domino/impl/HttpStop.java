package fr.asi.designer.anttasks.domino.impl;

import lotus.domino.NotesException;
import lotus.domino.Session;

import org.apache.tools.ant.BuildException;

import fr.asi.designer.anttasks.domino.BaseNotesTask;


/**
 * Ant task to stop an http task
 * @author Lionel HERVIER
 */
public class HttpStop extends BaseNotesTask {

	/**
	 * The server to send the command to
	 */
	private String server;
	
	/**
	 * Ne pas terminer en erreur si la commande console ne passe pas
	 */
	private boolean failSafe;
	
	/**
	 * @see fr.asi.designer.anttasks.domino.BaseNotesTask#execute(lotus.domino.Session)
	 */
	@Override
	public void execute(Session session) throws NotesException {
		this.log("Stopping HTTP task on server " + this.server);
		
		SendConsole task = this.delegate(SendConsole.class);
		task.setServer(this.server);
		task.setCommand("tell http quit");
		task.setTaskRunningMessage("HTTP Server");
		
		try {
			task.execute();
		} catch(BuildException e) {
			if( this.failSafe )
				return;
			throw e;
		}
	}
	
	// =======================================================================
	
	/**
	 * @param server the server to set
	 */
	public void setServer(String server) {
		this.server = server;
	}

	/**
	 * @param failSafe the failSafe to set
	 */
	public void setFailSafe(boolean failSafe) {
		this.failSafe = failSafe;
	}
}

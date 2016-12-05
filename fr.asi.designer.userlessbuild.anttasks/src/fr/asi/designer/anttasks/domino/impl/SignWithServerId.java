package fr.asi.designer.anttasks.domino.impl;

import lotus.domino.NotesException;
import lotus.domino.Session;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import fr.asi.designer.anttasks.domino.BaseDatabaseSetTask;
import fr.asi.designer.anttasks.domino.subtasks.CheckAdminRequestStatus;
import fr.asi.designer.anttasks.domino.subtasks.SignDatabaseWithServerId;

/**
 * Task to sign a given database with the server id
 * @author Lionel HERVIER
 */
public class SignWithServerId extends BaseDatabaseSetTask {

	/**
	 * @see fr.asi.designer.anttasks.domino.BaseDatabaseSetTask#execute(lotus.domino.Session, java.lang.String, java.lang.String)
	 */
	@Override
	public void execute(Session session, String server, String database) throws NotesException {
		this.log("Signing database '" + server + "!!" + database + "' with the server ID");
		
		// Create the adminp request
		SignDatabaseWithServerId signTask = this.delegate(SignDatabaseWithServerId.class);
		signTask.setServer(server);
		signTask.setDatabase(database);
		signTask.execute();
		String noteId = signTask.getNoteId();
		
		// Force adminp to run
		SendConsole sendConsole = this.delegate(SendConsole.class);
		sendConsole.setServer(server);
		sendConsole.setCommand("tell adminp process all");
		sendConsole.execute();
		
		// Wait for the request to finish
		int maxTimeout = 200;
		int tick = 0;
		String status = null;
		while( !"Processed".equals(status) && tick < maxTimeout ) {
			if( tick % 5 == 0 )
				this.log("Waiting for adminp requests to finish", Project.MSG_INFO);
			tick++;
			
			CheckAdminRequestStatus task = this.delegate(CheckAdminRequestStatus.class);
			task.setServer(server);
			task.setNoteId(noteId);
			task.execute();
			
			status = task.getStatus();
			
			if( "Error".equals(status) )
				throw new BuildException("The adminp request do not execute correctly. Check content of admin4.nsf database !");
			
			if( !"Processed".equals(status) )
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					throw new BuildException(e);
				}
		}
	}
}

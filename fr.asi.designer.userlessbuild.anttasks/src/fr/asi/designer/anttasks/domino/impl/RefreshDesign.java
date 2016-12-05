package fr.asi.designer.anttasks.domino.impl;

import lotus.domino.NotesException;
import lotus.domino.Session;

import org.apache.tools.ant.Project;

import fr.asi.designer.anttasks.domino.BaseDatabaseSetTask;
import fr.asi.designer.anttasks.util.Utils;

/**
 * Ant task to launch a refresh design of a set of databases.
 * 
 * Design refresh is done via a console command that load the "design" task
 * with parameters.
 * @author Lionel HERVIER
 */
public class RefreshDesign extends BaseDatabaseSetTask {

	/**
	 * Dry run ?
	 */
	private boolean dryRun = false;
	
	/**
	 * @param dryRun the dryRun to set
	 */
	public void setDryRun(boolean dryRun) {
		this.dryRun = dryRun;
	}

	/**
	 * @see fr.asi.designer.anttasks.domino.BaseDatabaseSetTask#execute(lotus.domino.Session, java.lang.String, java.lang.String)
	 */
	@Override
	public void execute(Session session, String server, String dbPath) throws NotesException {
		String cmd = "load design";
		if( !Utils.isEmpty(dbPath) ) { 
			this.log("Refreshing design of '" + server + "!!" + dbPath + "'", Project.MSG_INFO);
			cmd += " -f " + dbPath;
		} else
			this.log("Refreshing design of all databases on server '" + server + "'", Project.MSG_INFO);
		
		if( this.dryRun )
			return;
		
		SendConsole task = this.delegate(SendConsole.class);
		task.setServer(server);
		task.setCommand(cmd);
		task.setTaskRunningMessage("Designer");
		task.execute(session);
	}
}

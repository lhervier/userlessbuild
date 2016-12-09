package fr.asi.designer.anttasks.domino.impl;

import lotus.domino.NotesException;
import lotus.domino.Session;
import fr.asi.designer.anttasks.domino.BaseDatabaseSetTask;

/**
 * Task to send a "tell amgr run" on the server console
 * @author Lionel HERVIER
 */
public class TellAmgrRun extends BaseDatabaseSetTask {

	/**
	 * The name of the agent
	 */
	private String agent;
	
	/**
	 * @see fr.asi.designer.anttasks.domino.BaseDatabaseSetTask#execute(lotus.domino.Session, java.lang.String, java.lang.String)
	 */
	@Override
	protected void execute(Session session, String server, String dbPath) throws NotesException {
		SendConsole task = this.delegate(SendConsole.class);
		task.setServer(server);
		task.setCommand("tell amgr run \"" + dbPath + "\" '" + this.agent + "'");
		task.execute();
	}
	
	// ==================================================================================

	/**
	 * @param agent the agent to set
	 */
	public void setAgent(String agent) {
		this.agent = agent;
	}
}

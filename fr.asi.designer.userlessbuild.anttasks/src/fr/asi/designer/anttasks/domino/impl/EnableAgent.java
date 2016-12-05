package fr.asi.designer.anttasks.domino.impl;

import lotus.domino.Agent;
import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;

import org.apache.tools.ant.BuildException;

import fr.asi.designer.anttasks.domino.BaseDatabaseSetTask;
import fr.asi.designer.anttasks.util.Utils;

/**
 * Ant task to enable an agent on a set of databases
 * @author Lionel HERVIER
 */
public class EnableAgent extends BaseDatabaseSetTask {
	
	/**
	 * Agent to enable
	 */
	private String agent;
	
	/**
	 * The server to enable the agent on
	 */
	private String serverToRun;
	
	/**
	 * @param serverToRun the serverToRun to set
	 */
	public void setServerToRun(String serverToRun) {
		this.serverToRun = serverToRun;
	}

	/**
	 * @param agent the agent to set
	 */
	public void setAgent(String agent) {
		this.agent = agent;
	}

	/**
	 * @see fr.asi.designer.anttasks.domino.BaseDatabaseSetTask#execute(Session, String, String)
	 */
	@Override
	public void execute(Session session, String server, String dbPath) throws NotesException {
		this.log("Enabling agent '" + this.agent + "' in database '" + server + "!!" + dbPath + "'");
		Database src = null;
		try {
			src = this.openDatabase(server, dbPath);
			Agent ag = src.getAgent(EnableAgent.this.agent);
			if( ag == null )
				throw new BuildException("Agent '" + EnableAgent.this.agent + "' not found in database '" + server + "!!" + dbPath + "'");

			ag.setEnabled(true);
			ag.setServerName(EnableAgent.this.serverToRun);
			ag.save();
		} finally {
			Utils.recycleQuietly(src);
		}
	}
}

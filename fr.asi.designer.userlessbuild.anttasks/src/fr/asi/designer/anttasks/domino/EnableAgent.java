package fr.asi.designer.anttasks.domino;

import lotus.domino.Agent;
import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.Session;

import org.apache.tools.ant.BuildException;

import fr.asi.designer.anttasks.util.DominoUtils;

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
	 * @see fr.asi.designer.anttasks.domino.BaseDatabaseSetTask#execute(java.lang.String)
	 */
	@Override
	public void execute(final String dbPath) throws BuildException {
		try {
			this.log("Enabling agent '" + this.agent + "' in database '" + this.getServer() + "!!" + dbPath + "'");
			Runnable r = new Runnable() {
				public void run() {
					try {
						Session session = NotesFactory.createSession(
								(String) null, 
								(String) null, 
								(String) EnableAgent.this.getPassword()
						);
						Database src = session.getDatabase(EnableAgent.this.getServer(), dbPath, false);
						if( src == null )
							throw new RuntimeException("Database !" + EnableAgent.this.getServer() + "!!" + dbPath + " does not exists");
						if( !src.isOpen() )
							if( !src.open() )
								throw new RuntimeException("Unable to open database '" + EnableAgent.this.getServer() + "!!" + dbPath + "'");
						
						Agent ag = src.getAgent(EnableAgent.this.agent);
						if( ag == null )
							throw new RuntimeException("Agent '" + EnableAgent.this.agent + "' not found in database '" + EnableAgent.this.getServer() + "!!" + dbPath + "'");
						
						ag.setEnabled(true);
						ag.setServerName(EnableAgent.this.serverToRun);
						ag.save();
					} catch(NotesException e) {
						throw new RuntimeException(e);
					}
				}
			};
			DominoUtils.runInNotesThread(r);
		} catch(InterruptedException e) {
			throw new BuildException(e, this.getLocation());
		}
	}
}

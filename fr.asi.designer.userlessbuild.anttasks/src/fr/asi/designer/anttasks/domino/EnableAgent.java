package fr.asi.designer.anttasks.domino;

import lotus.domino.Agent;
import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.Session;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import fr.asi.designer.anttasks.util.DominoUtils;

/**
 * Ant task to enable an agent
 * @author Lionel HERVIER
 */
public class EnableAgent extends Task {
	
	/**
	 * Server
	 */
	private String server;
	
	/**
	 * Database
	 */
	private String database;
	
	/**
	 * Agent to enable
	 */
	private String agent;
	
	/**
	 * The server to enable the agent on
	 */
	private String serverToRun;
	
	/**
	 * Password of the local ID file
	 */
	private String password;

	/**
	 * @param serverToRun the serverToRun to set
	 */
	public void setServerToRun(String serverToRun) {
		this.serverToRun = serverToRun;
	}

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
	 * @param agent the agent to set
	 */
	public void setAgent(String agent) {
		this.agent = agent;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	/**
	 * Execution
	 */
	public void execute() throws BuildException {
		try {
			this.log("Enabling agent '" + this.agent + "' in database '" + this.server + "!!" + this.database + "'");
			
			Runnable r = new Runnable() {
				public void run() {
					try {
						Session session = NotesFactory.createSession(
								(String) null, 
								(String) null, 
								(String) EnableAgent.this.password
						);
						Database src = session.getDatabase(EnableAgent.this.server, EnableAgent.this.database, false);
						if( src == null )
							throw new RuntimeException("Database !" + EnableAgent.this.server + "!!" + EnableAgent.this.database + " does not exists");
						if( !src.isOpen() )
							if( !src.open() )
								throw new RuntimeException("Unable to open database '" + EnableAgent.this.server + "!!" + EnableAgent.this.database + "'");
						
						Agent ag = src.getAgent(EnableAgent.this.agent);
						if( ag == null )
							throw new RuntimeException("Agent '" + EnableAgent.this.agent + "' not found in database '" + EnableAgent.this.server + "!!" + EnableAgent.this.database + "'");
						
						ag.setEnabled(true);
						ag.setServerName(EnableAgent.this.serverToRun);
						ag.save();
					} catch(NotesException e) {
						throw new RuntimeException(e);
					}
				}
			};
			DominoUtils.runInNotesThread(r);
		} catch (InterruptedException e) {
			throw new BuildException(e, this.getLocation());
		}
	}

}

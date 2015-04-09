package fr.asi.designer.anttasks.domino;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NoteCollection;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.Session;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import fr.asi.designer.anttasks.util.DominoUtils;

/**
 * Ant task to declare that an agent must run on behalf of someone
 * @author Lionel HERVIER
 */
public class SetOnBehalfOf extends Task {

	/**
	 * Server
	 */
	private String server;
	
	/**
	 * Database
	 */
	private String database;
	
	/**
	 * Agent
	 */
	private String agent;
	
	/**
	 * On behalf of
	 */
	private String onBehalfOf;
	
	/**
	 * Password of the local ID file
	 */
	private String password;

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
	 * @param onBehalfOf the onBehalfOf to set
	 */
	public void setOnBehalfOf(String onBehalfOf) {
		this.onBehalfOf = onBehalfOf;
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
			this.log(this.server + "!!" + this.database + "/" + this.agent + " will be set to run on behalf of '" + this.onBehalfOf + "'");
	
			Runnable r = new Runnable() {
				public void run() {
					try {
						Session session = NotesFactory.createSession(
								(String) null, 
								(String) null, 
								(String) SetOnBehalfOf.this.password
						);
						
						// Ouvre la base
						Database db = session.getDatabase(SetOnBehalfOf.this.server, SetOnBehalfOf.this.database, false);
						if( db == null )
							throw new RuntimeException("Database '" + SetOnBehalfOf.this.server + "!!" + SetOnBehalfOf.this.database + "' does not exists");
						if( !db.isOpen() )
							if( !db.open() )
								throw new RuntimeException("Unable to open database '" + SetOnBehalfOf.this.server + "!!" + SetOnBehalfOf.this.database + "'");
						
						// Créé la collection
						NoteCollection coll = db.createNoteCollection(false);
						coll.setSelectAgents(true);
						coll.buildCollection();
						String id = coll.getFirstNoteID();
						while( id.length() > 0 ) {
							Document agentDoc = null;
							try {
								agentDoc = db.getDocumentByID(id);
								String title = agentDoc.getItemValueString("$TITLE");
								if( SetOnBehalfOf.this.agent.equals(title) ) {
									agentDoc.replaceItemValue("$OnBehalfOf", onBehalfOf);
									agentDoc.sign();
									agentDoc.save(true, false);
									break;
								}
							} finally {
								DominoUtils.recycleQuietly(agentDoc);
							}
							id = coll.getNextNoteID(id);
						}
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

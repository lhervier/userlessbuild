package fr.asi.designer.anttasks.domino;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NoteCollection;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.Session;

import org.apache.tools.ant.BuildException;

import fr.asi.designer.anttasks.util.DominoUtils;

/**
 * Ant task to declare that an agent must run on behalf of someone
 * @author Lionel HERVIER
 */
public class SetOnBehalfOf extends BaseDatabaseSetTask {

	/**
	 * Agent
	 */
	private String agent;
	
	/**
	 * On behalf of
	 */
	private String onBehalfOf;
	
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
	 * @see fr.asi.designer.anttasks.domino.BaseDatabaseSetTask#execute(java.lang.String)
	 */
	@Override
	public void execute(final String dbPath) throws BuildException {
		try {
			this.log(this.getServer() + "!!" + dbPath + "/" + this.agent + " will be set to run on behalf of '" + this.onBehalfOf + "'");
			Runnable r = new Runnable() {
				public void run() {
					try {
						Session session = NotesFactory.createSession(
								(String) null, 
								(String) null, 
								(String) SetOnBehalfOf.this.getPassword()
						);
						
						// Ouvre la base
						Database db = session.getDatabase(SetOnBehalfOf.this.getServer(), dbPath, false);
						if( db == null )
							throw new RuntimeException("Database '" + SetOnBehalfOf.this.getServer() + "!!" + dbPath + "' does not exists");
						if( !db.isOpen() )
							if( !db.open() )
								throw new RuntimeException("Unable to open database '" + SetOnBehalfOf.this.getServer() + "!!" + dbPath + "'");
						
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

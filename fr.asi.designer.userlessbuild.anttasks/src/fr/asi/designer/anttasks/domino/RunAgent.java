package fr.asi.designer.anttasks.domino;

import java.util.ArrayList;
import java.util.List;

import lotus.domino.Agent;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.Session;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import fr.asi.designer.anttasks.util.DominoUtils;

/**
 * Ant task to launch an agent. This task can contain
 * {@link ContextDocField} if you need to add values to the
 * document context.
 * @author Lionel HERVIER
 */
public class RunAgent extends Task {

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
	 * Password of the local Id file
	 */
	private String password;

	/**
	 * The fields to add
	 */
	private List<ContextDocField> contextDocFields = new ArrayList<ContextDocField>();
	
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
	 * @return an empty context doc field object
	 */
	public ContextDocField createContextDocField() {
		ContextDocField ret = new ContextDocField();
		this.contextDocFields.add(ret);
		return ret;
	}
	
	/**
	 * Execution
	 */
	public void execute() throws BuildException {
		try {
			this.log("Running agent '" + this.agent + "' in database '" + this.server + "!!" + this.database + "'");
			
			Runnable r = new Runnable() {
				public void run() {
					try {
						Session session = NotesFactory.createSession(
								(String) null, 
								(String) null, 
								(String) RunAgent.this.password
						);
						Database src = session.getDatabase(RunAgent.this.server, RunAgent.this.database, false);
						if( src == null )
							throw new RuntimeException("Database '" + RunAgent.this.server + "!!" + RunAgent.this.database + "' does not exists");
						if( !src.isOpen() )
							if( !src.open() )
								throw new RuntimeException("Unable to open database '" + RunAgent.this.server + "!!" + RunAgent.this.database + "'");
						
						Agent ag = src.getAgent(agent);
						if( ag == null )
							throw new RuntimeException("Unable to find agent '" + RunAgent.this.agent + " in database");
						
						Document doc = src.createDocument();
						try {
							doc.replaceItemValue("Form", "RunAgent");
							for( ContextDocField field : RunAgent.this.contextDocFields )
								doc.replaceItemValue(field.getName(), field.getValue());
							
							doc.save(true, false);
							String noteId = doc.getNoteID();
							
							ag.run(noteId);
						} finally {
							doc.remove(true);
						}
					} catch(NotesException e) {
						throw new RuntimeException(e);
					}
				}
			};
			DominoUtils.runInNotesThread(r);
		} catch (InterruptedException e) {
			throw new BuildException(e, this.getLocation());
		} finally {
			
		}
	}
	
}

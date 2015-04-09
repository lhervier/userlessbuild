package fr.asi.designer.anttasks.domino;

import lotus.domino.Database;
import lotus.domino.DocumentCollection;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.Session;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import fr.asi.designer.anttasks.util.DominoUtils;

/**
 * Ant task to clear the content of an update site database
 * @author Lionel HERVIER
 */
public class ClearUpdateSiteDb extends Task {

	/**
	 * The server where to find the update site db
	 */
	private String server;
	
	/**
	 * The database
	 */
	private String database;
	
	/**
	 * The password of the local ID file
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
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	/**
	 * Execution
	 */
	public void execute() throws BuildException {
		this.log("Clearing all content in database '" + this.server + "!!" + this.database + "'");
		try {
			Runnable r = new Runnable() {
				public void run() {
					try {
						Session session = NotesFactory.createSession(
								(String) null, 
								(String) null, 
								(String) ClearUpdateSiteDb.this.password
						);
						
						Database updateSite = session.getDatabase(ClearUpdateSiteDb.this.server, ClearUpdateSiteDb.this.database);
						if( updateSite == null )
							throw new RuntimeException("Unable to find '" + ClearUpdateSiteDb.this.server + "!!" + ClearUpdateSiteDb.this.database + "'");
						if( !updateSite.isOpen() )
							if( !updateSite.open() )
								throw new RuntimeException("Unable to open '" + ClearUpdateSiteDb.this.server + "!!" + ClearUpdateSiteDb.this.database + "'");
						
						DocumentCollection coll = updateSite.getAllDocuments();
						coll.removeAll(false);
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

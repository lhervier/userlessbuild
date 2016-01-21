package fr.asi.designer.anttasks.domino;

import java.util.ArrayList;
import java.util.List;

import lotus.domino.Database;
import lotus.domino.DbDirectory;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.Session;

import org.apache.tools.ant.BuildException;

import fr.asi.designer.anttasks.util.DominoUtils;
import fr.asi.designer.anttasks.util.Utils;

/**
 * A Set of domino databases
 * @author Lionel HERVIER
 */
public class DatabaseSet {

	/**
	 * The server to search for the databases
	 */
	private String server;
	
	/**
	 * The password to access the server
	 */
	private String password;
	
	/**
	 * A template name the databases must inherit from
	 */
	private String template;

	/**
	 * A database name
	 */
	private String database;
	
	/**
	 * @param server the server to set
	 */
	void setServer(String server) {
		this.server = server;
	}

	/**
	 * @param password the password to set
	 */
	void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @param template the template to set
	 */
	public void setTemplate(String template) {
		this.template = template;
	}
	
	/**
	 * @param database the database to set
	 */
	public void setDatabase(String database) {
		this.database = database;
	}

	/**
	 * Return the databases path
	 * @return the databases path
	 * @throws BuildException
	 */
	public List<String> getPaths() throws BuildException {
		final List<String> ret = new ArrayList<String>();
		
		if( !Utils.isEmpty(this.database) )
			ret.add(this.database);
		
		else if( !Utils.isEmpty(this.template) ){
			Runnable r = new Runnable() {
				public void run() {
					try {
						Session session = NotesFactory.createSession(
								(String) null, 
								(String) null, 
								(String) DatabaseSet.this.password
						);
						DbDirectory dir = session.getDbDirectory(DatabaseSet.this.server);
						Database db = dir.getFirstDatabase(DbDirectory.DATABASE);
						while( db != null ) {
							if( db.getDesignTemplateName().equals(DatabaseSet.this.template) )
								ret.add(db.getFilePath());
							db = dir.getNextDatabase();
						}
					} catch(NotesException e) {
						throw new RuntimeException(e);
					}
				}
			};
			try {
				DominoUtils.runInNotesThread(r);
			} catch (InterruptedException e) {
				throw new BuildException(e);
			}
		}
		return ret;
	}
}

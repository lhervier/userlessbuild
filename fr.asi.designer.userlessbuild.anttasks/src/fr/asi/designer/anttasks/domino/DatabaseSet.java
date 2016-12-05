package fr.asi.designer.anttasks.domino;

import java.util.ArrayList;
import java.util.List;

import lotus.domino.Database;
import lotus.domino.DbDirectory;
import lotus.domino.NotesException;
import lotus.domino.Session;
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
	 * A template name the databases must inherit from
	 */
	private String template;

	/**
	 * A database name
	 */
	private String database;
	
	/**
	 * The NotesSession
	 */
	private Session session;
	
	/**
	 * Return the databases path
	 * @return the databases path
	 * @throws NotesException
	 */
	public List<String> getPaths() throws NotesException {
		final List<String> ret = new ArrayList<String>();
		
		if( !Utils.isEmpty(this.database) )
			ret.add(this.database);
		
		else if( !Utils.isEmpty(this.template) ) {
			DbDirectory dir = null;
			try {
				dir = this.session.getDbDirectory(DatabaseSet.this.server);
				Database db = dir.getFirstDatabase(DbDirectory.DATABASE);
				while( db != null ) {
					if( db.getDesignTemplateName().equals(DatabaseSet.this.template) )
						ret.add(db.getFilePath());
					db = dir.getNextDatabase();
				}
			} finally {
				Utils.recycleQuietly(dir);
			}
		}
		return ret;
	}
	
	// ===============================================================================================
	
	/**
	 * @param session the session to set
	 */
	void setSession(Session session) {
		this.session = session;
	}

	/**
	 * @param server the server to set
	 */
	void setServer(String server) {
		this.server = server;
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
}

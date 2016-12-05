package fr.asi.designer.anttasks.domino.subtasks;

import org.apache.tools.ant.BuildException;

import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;
import fr.asi.designer.anttasks.domino.BaseNotesTask;
import fr.asi.designer.anttasks.util.Utils;

/**
 * Task to copy a database. Will only use the existing API to copy
 * a source to a destination so :
 * - Will fail if the destination database already exists.
 * - Documents won't be copied
 * - If a template with the same name already exists on the destination server, the destination database
 *   will NOT be flagged as a template
 * @author Lionel HERVIER
 */
public class DatabaseCopy extends BaseNotesTask {

	/**
	 * The source server
	 */
	private String srcServer;
	
	/**
	 * The source database
	 */
	private String srcDatabase;
	
	/**
	 * The destination server
	 */
	private String destServer;
	
	/**
	 * The destination database
	 */
	private String destDatabase;
	
	/**
	 * @see fr.asi.designer.anttasks.domino.BaseNotesTask#execute(lotus.domino.Session)
	 */
	@Override
	public void execute(Session session) throws NotesException {
		this.log("Copying database '" + this.srcServer + "!!" + this.srcDatabase + "' to '" + this.destServer + "!!" + this.destDatabase + "'");
		
		Database src = null;
		Database dest = null;
		try {
			src = this.openDatabase(
					this.srcServer, 
					this.srcDatabase
			);
			
			dest = session.getDatabase(destServer, destDatabase, false);
			if( dest != null )
				throw new BuildException("Database '" + destServer + "!!" + destDatabase + "' already exists");
			
			// Copie la base source
			dest = src.createCopy(destServer, destDatabase);
			if( !dest.isOpen() )
				if( !dest.open() )
					throw new BuildException("Unable to open the database i just copied...");
		} finally {
			Utils.recycleQuietly(dest);
			Utils.recycleQuietly(src);
		}
	}

	// ==========================================================================
	
	/**
	 * @param srcServer the srcServer to set
	 */
	public void setSrcServer(String srcServer) {
		this.srcServer = srcServer;
	}

	/**
	 * @param srcDatabase the srcDatabase to set
	 */
	public void setSrcDatabase(String srcDatabase) {
		this.srcDatabase = srcDatabase;
	}

	/**
	 * @param destServer the destServer to set
	 */
	public void setDestServer(String destServer) {
		this.destServer = destServer;
	}

	/**
	 * @param destDatabase the destDatabase to set
	 */
	public void setDestDatabase(String destDatabase) {
		this.destDatabase = destDatabase;
	}
}

package fr.asi.designer.anttasks.domino.subtasks;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.NotesException;
import lotus.domino.Session;
import fr.asi.designer.anttasks.domino.BaseNotesTask;
import fr.asi.designer.anttasks.util.Utils;

/**
 * Copy all documents from a source database to a destination
 * @author Lionel HERVIER
 */
public class CopyAllDocuments extends BaseNotesTask {

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
		this.log("Copying all documents from '" + this.srcServer + "!!" + this.srcDatabase + "' to '" + this.destServer + "!!" + this.destDatabase + "'");
		
		Database src = null;
		Database dest = null;
		DocumentCollection coll = null;
		try {
			src = this.openDatabase(srcServer, srcDatabase);
			dest = this.openDatabase(destServer, destDatabase);
			
			coll = src.getAllDocuments();
			Document doc = coll.getFirstDocument();
			while( doc != null ) {
				Document copy = doc.copyToDatabase(dest);
				copy.save(true, false);
				copy.recycle();
				
				Document tmp = coll.getNextDocument(doc);
				doc.recycle();
				doc = tmp;
			}
		} finally {
			Utils.recycleQuietly(coll);
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

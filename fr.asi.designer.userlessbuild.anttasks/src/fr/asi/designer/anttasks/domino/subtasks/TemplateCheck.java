package fr.asi.designer.anttasks.domino.subtasks;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NoteCollection;
import lotus.domino.NotesException;
import lotus.domino.Session;

import org.apache.tools.ant.BuildException;

import fr.asi.designer.anttasks.domino.BaseNotesTask;
import fr.asi.designer.anttasks.util.Utils;

/**
 * Check if the given database is a template.
 * @author Lionel HERVIER
 */
public class TemplateCheck extends BaseNotesTask {

	/**
	 * Server where to find the database
	 */
	private String server;
	
	/**
	 * Name of the database to remove
	 */
	private String database;
	
	/**
	 * @see fr.asi.designer.anttasks.domino.BaseNotesTask#execute(lotus.domino.Session)
	 */
	@Override
	public void execute(Session session) throws NotesException {
		this.log("Checking if " + this.server + "!!" + this.database + " is a template " + Thread.currentThread().toString());
		
		Database src = null;
		NoteCollection nc = null;
		try {
			src = this.openDatabase(this.server, this.database);
			
			nc = src.createNoteCollection(false);
			nc.setSelectIcon(true);
			nc.buildCollection();

			String noteid = nc.getFirstNoteID();
			Document icon = src.getDocumentByID(noteid);
			String title = icon.getItemValueString("$Title");
			
			int pos = title.indexOf("\n#1");
			if( pos == -1 )
				throw new BuildException("Not a template...");
		} finally {
			Utils.recycleQuietly(nc);
			Utils.recycleQuietly(src);
		}
	}
	
	// =========================================================================
	
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
}

package fr.asi.designer.anttasks.domino.impl;

import lotus.domino.NotesException;
import lotus.domino.Session;
import fr.asi.designer.anttasks.domino.BaseNotesTask;
import fr.asi.designer.anttasks.domino.subtasks.CopyAllDocuments;
import fr.asi.designer.anttasks.domino.subtasks.TemplateCheck;

/**
 * Ant task to copy a domino database. This task delegates to multiple other tasks.
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
	 * After copying the database, the task will check if the
	 * destination database defines this template name. 
	 */
	private String templateCheck;
	
	/**
	 * @see fr.asi.designer.anttasks.domino.BaseNotesTask#execute(lotus.domino.Session)
	 */
	@Override
	public void execute(Session session) throws NotesException {
		// Remove destination database
		DatabaseDelete deleteTask = this.delegate(DatabaseDelete.class);
		deleteTask.setServer(this.destServer);
		deleteTask.setDatabase(this.destDatabase);
		deleteTask.execute();
		
		// Copy source to destination
		fr.asi.designer.anttasks.domino.subtasks.DatabaseCopy copyTask = this.delegate(fr.asi.designer.anttasks.domino.subtasks.DatabaseCopy.class);
		copyTask.setSrcServer(this.srcServer);
		copyTask.setSrcDatabase(this.srcDatabase);
		copyTask.setDestServer(this.destServer);
		copyTask.setDestDatabase(this.destDatabase);
		copyTask.execute();
		
		// Copy all documents
		CopyAllDocuments copyDocsTask = this.delegate(CopyAllDocuments.class);
		copyDocsTask.setSrcServer(this.srcServer);
		copyDocsTask.setSrcDatabase(this.srcDatabase);
		copyDocsTask.setDestServer(this.destServer);
		copyDocsTask.setDestDatabase(this.destDatabase);
		copyDocsTask.execute();
		
		// Check if destination database is a template
		if( this.templateCheck != null && this.templateCheck.length() != 0 ) {
			TemplateCheck tmplCheckTask = this.delegate(TemplateCheck.class);
			tmplCheckTask.setServer(this.destServer);
			tmplCheckTask.setDatabase(this.destDatabase);
			tmplCheckTask.execute();
		}
	}
	
	// ==================================================================================================
	
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

	/**
	 * @param templateCheck the templateCheck to set
	 */
	public void setTemplateCheck(String templateCheck) {
		this.templateCheck = templateCheck;
	}
}

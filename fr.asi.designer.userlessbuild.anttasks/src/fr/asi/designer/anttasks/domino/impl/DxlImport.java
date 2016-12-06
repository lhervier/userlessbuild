package fr.asi.designer.anttasks.domino.impl;

import java.io.File;

import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.Stream;

import org.apache.tools.ant.BuildException;

import fr.asi.designer.anttasks.domino.BaseNotesTask;
import fr.asi.designer.anttasks.util.Utils;

/**
 * Import a dxl file into a database
 * @author Lionel HERVIER
 */
public class DxlImport extends BaseNotesTask {

	/**
	 * Success log message
	 */
	private static final String IMPORT_SUCCESS = "<?xml version='1.0'?>\n" +
				"<DXLImporterLog>\n" +
				"</DXLImporterLog>";
	
	/**
	 * The server
	 */
	private String server;
	
	/**
	 * The database
	 */
	private String database;
	
	/**
	 * The file to get the DXL from
	 */
	private String fromFile;
	
	/**
	 * @see fr.asi.designer.anttasks.domino.BaseNotesTask#execute(lotus.domino.Session)
	 */
	@Override
	protected void execute(Session session) throws NotesException {
		Database db = null;
		Stream stream = null;
		lotus.domino.DxlImporter importer = null;
		try {
			db = this.openDatabase(this.server, this.database);
			
			File f = new File(this.getProject().getProperty("basedir") + "/" + this.fromFile);
			stream = session.createStream();
			if ( !stream.open(f.getAbsolutePath()) || (stream.getBytes() == 0) )
				throw new BuildException("Unable to open file " + f.getAbsolutePath());
			
			importer = session.createDxlImporter();
			importer.setReplaceDbProperties(false);
			importer.setReplicaRequiredForReplaceOrUpdate(false);
			importer.importDxl(stream, db);

			String logs = importer.getLog();
			if( !logs.equals(IMPORT_SUCCESS) )
				throw new BuildException(logs);
		} finally {
			Utils.closeQuietly(stream);
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

	/**
	 * @param fromFile the fromFile to set
	 */
	public void setFromFile(String fromFile) {
		this.fromFile = fromFile;
	}

}

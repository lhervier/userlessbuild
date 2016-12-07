package fr.asi.designer.anttasks.domino.impl;

import java.io.File;

import lotus.domino.Database;
import lotus.domino.DxlImporter;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.Stream;

import org.apache.tools.ant.BuildException;

import fr.asi.designer.anttasks.domino.BaseDatabaseSetTask;
import fr.asi.designer.anttasks.util.Utils;

/**
 * Import a dxl file into a database
 * @author Lionel HERVIER
 */
public class DxlImport extends BaseDatabaseSetTask {

	/**
	 * Success log message
	 */
	private static final String IMPORT_SUCCESS = "<?xml version='1.0'?>\n" +
				"<DXLImporterLog>\n" +
				"</DXLImporterLog>";
	
	/**
	 * The file to get the DXL from
	 */
	private String fromFile;
	
	/**
	 * @see fr.asi.designer.anttasks.domino.BaseDatabaseSetTask#execute(lotus.domino.Session, java.lang.String, java.lang.String)
	 */
	@Override
	protected void execute(Session session, String server, String database) throws NotesException {
		this.log("Importing " + this.fromFile + " to " + server + "!!" + database);
		
		Database db = null;
		Stream stream = null;
		DxlImporter importer = null;
		try {
			db = this.openDatabase(server, database);
			
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
			Utils.recycleQuietly(importer);
			Utils.recycleQuietly(db);
			Utils.closeQuietly(stream);
		}
	}
	
	// =========================================================================

	/**
	 * @param fromFile the fromFile to set
	 */
	public void setFromFile(String fromFile) {
		this.fromFile = fromFile;
	}

}

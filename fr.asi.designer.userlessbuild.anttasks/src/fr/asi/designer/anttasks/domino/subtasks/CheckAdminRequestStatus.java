package fr.asi.designer.anttasks.domino.subtasks;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.NotesException;
import lotus.domino.Session;
import fr.asi.designer.anttasks.domino.BaseNotesTask;
import fr.asi.designer.anttasks.util.Utils;

/**
 * Check the status of a given adminp request
 * @author Lionel HERVIER
 */
public class CheckAdminRequestStatus extends BaseNotesTask {

	/**
	 * The server
	 */
	private String server;
	
	/**
	 * The noteId of the adminp request
	 */
	private String noteId;
	
	/**
	 * The status of the request
	 */
	private String status;
	
	/**
	 * @see fr.asi.designer.anttasks.domino.BaseNotesTask#execute(lotus.domino.Session)
	 */
	@Override
	protected void execute(Session session) throws NotesException {
		Database admin4 = null;
		Document request = null;
		DocumentCollection responses = null;
		Document response = null;
		try {
			admin4 = this.openDatabase(this.server, "admin4.nsf");
			request = admin4.getDocumentByID(this.noteId);
			
			responses = request.getResponses();
			if( responses.getCount() == 0 )
				return;
			response = responses.getFirstDocument();
			
			String progress = response.getItemValueString("AdminPInProgress");
			if( progress.length() != 0 ) {
				this.status = "AdminPInProgress";
				return;
			}
			
			String errorFlag = response.getItemValueString("ErrorFlag");
			if( errorFlag.length() == 0 || "Processed".equals(errorFlag) ) {
				this.status = "Processed";
				return;
			}
			
			this.status = "Error";
		} finally {
			Utils.recycleQuietly(response);
			Utils.recycleQuietly(responses);
			Utils.recycleQuietly(request);
			Utils.recycleQuietly(admin4);
		}
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	// ===============================================================================
	
	/**
	 * @param server the server to set
	 */
	public void setServer(String server) {
		this.server = server;
	}

	/**
	 * @param noteId the noteId to set
	 */
	public void setNoteId(String noteId) {
		this.noteId = noteId;
	}
}

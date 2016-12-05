package fr.asi.designer.anttasks.domino;

import lotus.domino.NotesException;
import lotus.domino.Session;

import org.apache.tools.ant.Project;

/**
 * Send a command on a domino server
 * 
 * @author Lionel HERVIER & Philippe ARDIT
 */
public class SendConsole extends BaseNotesTask {

	/**
	 * The server
	 */
	private String server;

	/**
	 * The command to send
	 */
	private String command;

	/**
	 * @param server the server to set
	 */
	public void setServer(String server) {
		this.server = server;
	}

	/**
	 * @param command the command to set
	 */
	public void setCommand(String command) {
		this.command = command;
	}

	/**
	 * @see fr.asi.designer.anttasks.domino.BaseNotesTask#execute(lotus.domino.Session)
	 */
	@Override
	public void execute(Session session) throws NotesException {
		this.log("Sending the command '" + this.command + "' on server '" + this.server + "'", Project.MSG_INFO);
		session.sendConsoleCommand(
				this.server, 
				this.command
		);
	}
}

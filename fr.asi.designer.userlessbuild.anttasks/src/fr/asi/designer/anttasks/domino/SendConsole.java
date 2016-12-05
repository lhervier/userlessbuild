package fr.asi.designer.anttasks.domino;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import fr.asi.designer.anttasks.util.ConsoleException;
import fr.asi.designer.anttasks.util.DominoUtils;

/**
 * Send a command on a domino server
 * 
 * @author Lionel HERVIER & Philippe ARDIT
 */
public class SendConsole extends Task {

	/**
	 * The server
	 */
	private String server;

	/**
	 * Password of the local id file
	 */
	private String password;

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
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @param command the command to set
	 */
	public void setCommand(String command) {
		this.command = command;
	}

	/**
	 * Execution
	 */
	public void execute() {
		try {
			this.log("Sending the command '" + this.command + "' on server '" + this.server + "'", Project.MSG_INFO);

			DominoUtils.sendConsole(this.server, this.command, this.password);

			this.log("Command '" + this.command + "' sent on server '" + this.server + "'", Project.MSG_INFO);
		} catch (ConsoleException e) {
			this.log(e, Project.MSG_ERR);
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			this.log(e, Project.MSG_ERR);
			throw new RuntimeException(e);
		}
	}
}

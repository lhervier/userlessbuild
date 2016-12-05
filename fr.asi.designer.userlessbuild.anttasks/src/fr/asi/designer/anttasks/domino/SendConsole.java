package fr.asi.designer.anttasks.domino;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import lotus.domino.NotesException;
import lotus.domino.Session;

import org.apache.tools.ant.BuildException;
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
	 * The name of the command to check for shutdown
	 */
	private String commandName;

	/**
	 * Timeout waiting for the task to end
	 */
	private int timeout = 200 * 1000;
	
	/**
	 * @see fr.asi.designer.anttasks.domino.BaseNotesTask#execute(lotus.domino.Session)
	 */
	@Override
	public void execute(Session session) throws NotesException {
		this.log("Sending command '" + this.command + "' to server '" + this.server + "'", Project.MSG_INFO);
		session.sendConsoleCommand(
				this.server, 
				this.command
		);
		
		if( this.commandName == null || this.commandName.length() == 0 ) {
			this.log("Command launched... please check manually", Project.MSG_INFO);
			return;
		}
		
		try {
			boolean finished = false;
			long end = System.currentTimeMillis() + this.timeout;
			int tick = 0;
			while( !finished && System.currentTimeMillis() < end ) {
				if( tick % 5 == 0 )
					this.log("Waiting for " + this.commandName + " task to shutdown", Project.MSG_INFO);
				tick++;
				
				Thread.sleep(1000);
				
				String tasks = session.sendConsoleCommand(
						this.server, 
						"show task"
				);
				
				StringReader reader = new StringReader(tasks);
				BufferedReader breader = new BufferedReader(reader);
				String line = breader.readLine();
				finished = true;
				while( finished && line != null ) {
					if( line.indexOf(this.commandName) != -1 )
						finished = false;
					else
						line = breader.readLine();
				}
			}
			if( System.currentTimeMillis() >= end )
				throw new BuildException("Unable to detect the end of the " + this.commandName + " task");
		} catch(IOException e) {
			throw new BuildException(e);
		} catch (InterruptedException e) {
			throw new BuildException(e);
		}
		this.log(this.commandName + " task stopped", Project.MSG_INFO);
	}
	
	// ==============================================================================
	
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
	 * @param commandName the commandName to set
	 */
	public void setCommandName(String commandName) {
		this.commandName = commandName;
	}

	/**
	 * @param timeout the timeout to set
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
}

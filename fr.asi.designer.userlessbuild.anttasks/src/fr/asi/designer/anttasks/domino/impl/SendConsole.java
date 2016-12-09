package fr.asi.designer.anttasks.domino.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lotus.domino.NotesException;
import lotus.domino.Session;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import fr.asi.designer.anttasks.domino.BaseNotesTask;
import fr.asi.designer.anttasks.util.Utils;

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
	 * If this line is found in show task, then the task is still running.
	 */
	private String taskRunningMessage;
	
	/**
	 * While this line is not found in "show tasks", the task is still starting.
	 */
	private String taskStartedMessage;
	
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
		String taskMsg = session.sendConsoleCommand(
				this.server, 
				this.command
		);
		this.log(taskMsg);
		
		if( Utils.isEmpty(this.taskRunningMessage) && Utils.isEmpty(this.taskStartedMessage) ) {
			this.log("Command launched... please check manually", Project.MSG_INFO);
			return;
		}
		
		Pattern runningPattern = this.taskRunningMessage == null ? null : Pattern.compile(this.taskRunningMessage);
		Pattern startedPattern = this.taskStartedMessage == null ? null : Pattern.compile(this.taskStartedMessage);
		
		try {
			boolean running = !Utils.isEmpty(this.taskRunningMessage);
			boolean started = Utils.isEmpty(this.taskStartedMessage);
			
			long end = System.currentTimeMillis() + this.timeout;
			int tick = 0;
			while( (running || !started) && System.currentTimeMillis() < end ) {
				if( tick % 5 == 0 )
					this.log("Waiting for task to start/finish", Project.MSG_INFO);
				tick++;
				
				Thread.sleep(1000);
				
				String tasks = session.sendConsoleCommand(
						this.server, 
						"show task"
				);
				
				if( running && runningPattern != null ) {
					StringReader reader = new StringReader(tasks);
					BufferedReader breader = new BufferedReader(reader);
					String line = breader.readLine();
					running = false;
					while( line != null ) {
						Matcher m = runningPattern.matcher(line);
						if( m.find() ) {
							running = true;
							break;
						}
						line = breader.readLine();
					}
				}
				
				if( !started && startedPattern != null ) {
					StringReader reader = new StringReader(tasks);
					BufferedReader breader = new BufferedReader(reader);
					String line = breader.readLine();
					started = false;
					while( line != null ) {
						Matcher m = startedPattern.matcher(line);
						if( m.find() ) {
							started = true;
							break;
						}
						line = breader.readLine();
					}
				}
			}
			if( System.currentTimeMillis() >= end )
				throw new BuildException("Timeout monitoring task");
		} catch(IOException e) {
			throw new BuildException(e);
		} catch (InterruptedException e) {
			throw new BuildException(e);
		}
		this.log("Task finished/started", Project.MSG_INFO);
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
	 * @param taskRunning the commandName to set
	 */
	public void setTaskRunningMessage(String taskRunning) {
		this.taskRunningMessage = taskRunning;
	}

	/**
	 * @param taskStartedMessage the taskStarted to set
	 */
	public void setTaskStartedMessage(String taskStartedMessage) {
		this.taskStartedMessage = taskStartedMessage;
	}

	/**
	 * @param timeout the timeout to set
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
}

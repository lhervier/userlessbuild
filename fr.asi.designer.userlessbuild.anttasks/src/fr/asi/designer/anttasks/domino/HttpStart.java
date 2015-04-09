package fr.asi.designer.anttasks.domino;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import fr.asi.designer.anttasks.util.DominoUtils;

/**
 * Starts an http task on a domino server
 * @author Lionel HERVIER
 */
public class HttpStart extends Task {

	/**
	 * The server
	 */
	private String server;
	
	/**
	 * Password of the local id file
	 */
	private String password;

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
	 * Execution
	 */
	public void execute() {
		try {
			this.log("Starting HTTP Task on server '" + this.server + "'", Project.MSG_INFO);
			
			DominoUtils.sendConsole(
					this.server, 
					"load http", 
					this.password
			);
			
			boolean ok = false;
			int timeout = 0;
			while( !ok && timeout < 200 ) {
				if( timeout % 5 == 0 )
					this.log("Waiting for http task to start", Project.MSG_INFO);
				Thread.sleep(1000);
				
				String tasks = DominoUtils.sendConsole(this.server, "show task", this.password);
				StringReader reader = new StringReader(tasks);
				BufferedReader breader = new BufferedReader(reader);
				String line = breader.readLine();
				while( !ok && line != null ) {
					if( line.indexOf("HTTP Server") != -1 && line.indexOf("Listen for connect requests on TCP Port:") != -1 )
						ok = true;
					else
						line = breader.readLine();
				}
				timeout++;
			}
			if( timeout == 200 )
				throw new RuntimeException("Unable to start http task...");
			this.log("HTTP Task started", Project.MSG_INFO);
		} catch (InterruptedException e) {
			this.log(e, Project.MSG_ERR);
			throw new RuntimeException(e);
		} catch (IOException e) {
			this.log(e, Project.MSG_ERR);
			throw new RuntimeException(e);
		}
	}
}

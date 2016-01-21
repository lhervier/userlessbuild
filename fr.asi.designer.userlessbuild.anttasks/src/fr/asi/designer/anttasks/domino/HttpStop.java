package fr.asi.designer.anttasks.domino;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import fr.asi.designer.anttasks.util.ConsoleException;
import fr.asi.designer.anttasks.util.DominoUtils;

/**
 * Ant task to stop an http task
 * @author Lionel HERVIER
 */
public class HttpStop extends Task {

	/**
	 * The server to send the command to
	 */
	private String server;
	
	/**
	 * The password of the local id file
	 */
	private String password;
	
	/**
	 * Ne pas terminer en erreur si la commande console ne passe pas
	 */
	private boolean failSafe;
	
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
	 * @param failSafe the failSafe to set
	 */
	public void setFailSafe(boolean failSafe) {
		this.failSafe = failSafe;
	}

	/**
	 * Execution
	 */
	public void execute() {
		try {
			this.log("Stopping HTTP task on server " + this.server);
			
			DominoUtils.sendConsole(this.server, "tell http quit", this.password);
			
			boolean ok = false;
			int timeout = 0;
			while( !ok && timeout < 200 ) {
				if( timeout % 5 == 0 )
					this.log("httpStop : Waiting for http task to shutdown", Project.MSG_INFO);
				Thread.sleep(1000);
				
				String tasks = DominoUtils.sendConsole(this.server, "show task", this.password);
				StringReader reader = new StringReader(tasks);
				BufferedReader breader = new BufferedReader(reader);
				String line = breader.readLine();
				ok = true;
				while( ok && line != null ) {
					if( line.indexOf("HTTP Server") != -1 )
						ok = false;
					else
						line = breader.readLine();
				}
				timeout++;
			}
			if( timeout == 200 )
				throw new RuntimeException("Unable to detect http task shutdown");
			this.log("HTTP Task stopped", Project.MSG_INFO);
		} catch (ConsoleException e) {
			this.log(e, Project.MSG_ERR);
			if( this.failSafe )
				return;
			else
				throw new RuntimeException(e);
		} catch (InterruptedException e) {
			this.log(e, Project.MSG_ERR);
			throw new RuntimeException(e);
		} catch (IOException e) {
			this.log(e, Project.MSG_ERR);
			throw new RuntimeException(e);
		}
	}
}

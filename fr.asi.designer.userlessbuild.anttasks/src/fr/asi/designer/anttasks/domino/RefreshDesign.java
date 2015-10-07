package fr.asi.designer.anttasks.domino;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import fr.asi.designer.anttasks.util.ConsoleException;
import fr.asi.designer.anttasks.util.DominoUtils;

/**
 * Ant task to launch a refresh design of the given database.
 * Design refresh is done via a console command that load the "design" task
 * with parameters.
 * @author Lionel HERVIER
 */
public class RefreshDesign extends Task {

	/**
	 * The server
	 */
	private String server;
	
	/**
	 * The database
	 */
	private String database;
	
	/**
	 * The password of the local ID file
	 */
	private String password;

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
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	/**
	 * Execution
	 */
	public void execute() throws BuildException {
		try {
			this.log("Refreshing design of '" + this.server + "!!" + this.database + "'", Project.MSG_INFO);
			
			DominoUtils.sendConsole(
					this.server, 
					"load design -f " + this.database, 
					this.password
			);
			
			boolean ok = false;
			int timeout = 0;
			while( !ok && timeout < 200 ) {
				if( timeout % 5 == 0 )
					this.log("Waiting for Designer task to shutdown", Project.MSG_INFO);
				Thread.sleep(1000);
				
				String tasks = DominoUtils.sendConsole(this.server, "show task", this.password);
				StringReader reader = new StringReader(tasks);
				BufferedReader breader = new BufferedReader(reader);
				String line = breader.readLine();
				ok = true;
				while( ok && line != null ) {
					if( line.indexOf("Designer") != -1 )
						ok = false;
					else
						line = breader.readLine();
				}
			}
			if( timeout == 200 )
				throw new RuntimeException("Unable to detect the end of the designer domino task");
			this.log("Designer task stopped", Project.MSG_INFO);
		} catch (ConsoleException e) {
			throw new BuildException(e, this.getLocation());
		} catch (InterruptedException e) {
			throw new BuildException(e, this.getLocation());
		} catch (IOException e) {
			throw new BuildException(e, this.getLocation());
		}
	}
}

package fr.asi.designer.anttasks.domino;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import fr.asi.designer.anttasks.util.ConsoleException;
import fr.asi.designer.anttasks.util.DominoUtils;
import fr.asi.designer.anttasks.util.Utils;

/**
 * Ant task to launch a refresh design of a set of databases.
 * 
 * Design refresh is done via a console command that load the "design" task
 * with parameters.
 * @author Lionel HERVIER
 */
public class RefreshDesign extends BaseDatabaseSetTask {

	/**
	 * Dry run ?
	 */
	private boolean dryRun = false;
	
	/**
	 * @param dryRun the dryRun to set
	 */
	public void setDryRun(boolean dryRun) {
		this.dryRun = dryRun;
	}

	/**
	 * @see fr.asi.designer.anttasks.domino.BaseDatabaseSetTask#execute(java.lang.String)
	 */
	@Override
	public void execute(final String dbPath) throws BuildException {
		try {
			String cmd = "load design";
			if( !Utils.isEmpty(dbPath) ) { 
				this.log("Refreshing design of '" + this.getServer() + "!!" + dbPath + "'", Project.MSG_INFO);
				cmd += " -f " + dbPath;
			} else {
				this.log("Refreshing design of all databases on server '" + this.getServer() + "'", Project.MSG_INFO);
			}
			
			if( this.dryRun )
				return;
			
			DominoUtils.sendConsole(
					this.getServer(), 
					cmd, 
					this.getPassword()
			);
			
			boolean ok = false;
			int timeout = 0;
			while( !ok && timeout < 200 ) {
				if( timeout % 5 == 0 )
					this.log("Waiting for Designer task to shutdown", Project.MSG_INFO);
				Thread.sleep(1000);
				
				String tasks = DominoUtils.sendConsole(this.getServer(), "show task", this.getPassword());
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

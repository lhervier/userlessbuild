package fr.asi.designer.anttasks.domino;

import lotus.domino.AdministrationProcess;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.Session;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import fr.asi.designer.anttasks.util.BooleanHolder;
import fr.asi.designer.anttasks.util.ConsoleException;
import fr.asi.designer.anttasks.util.DominoUtils;
import fr.asi.designer.anttasks.util.StringHolder;

/**
 * Task to sign a given database with the server id
 * @author Lionel HERVIER
 */
public class SignWithServerId extends Task {

	/**
	 * The server to find the database
	 */
	private String server;
	
	/**
	 * The database to sign
	 */
	private String database;
	
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
			this.log("Signing database '" + this.server + "!!" + this.database + "' with the server ID");
			
			final StringHolder noteId = new StringHolder();
			
			// Créé le doc dans la base admin4.nsf
			Runnable r = new Runnable() {
				public void run() {
					try {
						Session session = NotesFactory.createSession(
								(String) null, 
								(String) null, 
								(String) SignWithServerId.this.password
						);
						AdministrationProcess ap = session.createAdministrationProcess(server);
						noteId.s = ap.signDatabaseWithServerID(
								SignWithServerId.this.server, 
								SignWithServerId.this.database, 
								false
						);
					} catch(NotesException e) {
						throw new RuntimeException(e);
					}
				}
			};
			DominoUtils.runInNotesThread(r);
			
			// Force un lancement d'adminp
			DominoUtils.sendConsole(
					this.server, 
					"tell adminp process all", 
					this.password
			);
			
			// Attend que la requête soit traitée
			int maxTimeout = 200;
			int timeout = 0;
			final BooleanHolder ok = new BooleanHolder();
			while( !ok.b && timeout < maxTimeout ) {
				r = new Runnable() {
					public void run() {
						try {
							Session session = NotesFactory.createSession(
									(String) null, 
									(String) null, 
									(String) SignWithServerId.this.password
							);
							
							Database admin4 = session.getDatabase(SignWithServerId.this.server, "admin4.nsf");
							if( admin4 == null )
								throw new RuntimeException("Unable to find admin4.nsf on server " + SignWithServerId.this.server);
							if( !admin4.isOpen() )
								if( !admin4.open() )
									throw new RuntimeException("Unable to open admin4.nsf on server " + SignWithServerId.this.server);
							
							Document request = null;
							DocumentCollection responses = null;
							Document response = null;
							try {
								request = admin4.getDocumentByID(noteId.s);
								
								responses = request.getResponses();
								if( responses.getCount() == 0 )
									return;
								response = responses.getFirstDocument();
								
								String progress = response.getItemValueString("AdminPInProgress");
								if( progress.length() != 0 )
									return;
								
								String errorFlag = response.getItemValueString("ErrorFlag");
								if( errorFlag.length() == 0 || errorFlag.equals("Processed") ) {
									ok.b = true;
									return;
								}
								
								throw new RuntimeException("At least one of the adminp request do not execute correctly. Check content of admin4.nsf database !");
							} finally {
								DominoUtils.recycleQuietly(response);
								DominoUtils.recycleQuietly(responses);
								DominoUtils.recycleQuietly(request);
							}
						} catch(NotesException e) {
							throw new RuntimeException(e);
						}
					}
				};
				DominoUtils.runInNotesThread(r);
				if( timeout % 5 == 0 )
					this.log("Waiting for adminp requests to finish", Project.MSG_INFO);
				timeout++;
				Thread.sleep(1000);
			}
			if( !ok.b )
				throw new BuildException("Adminp request was NOT processed... Please, check admin4.nsf.");
		} catch (ConsoleException e) {
			throw new BuildException(e, this.getLocation());
		} catch (InterruptedException e) {
			throw new BuildException(e, this.getLocation());
		} finally {
			
		}
	}
}

package fr.asi.designer.anttasks.domino;

import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.NotesThread;
import lotus.domino.Session;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import fr.asi.designer.anttasks.util.ObjectHolder;
import fr.asi.designer.anttasks.util.Utils;

/**
 * A task that runs inside a Notes Session
 * @author Lionel HERVIER
 */
public abstract class BaseNotesTask extends Task {

	/**
	 * Password of the local id file
	 */
	private String password;
	
	/**
	 * The notes session
	 */
	private Session session;
	
	/**
	 * Execution
	 * @throws NotesException in cas of trouble...
	 */
	protected abstract void execute(Session session) throws NotesException;
	
	/**
	 * Execution
	 */
	public final void execute() {
		final ObjectHolder<Throwable> exHolder = new ObjectHolder<Throwable>();
		Thread t = new NotesThread() {
			public void runNotes() {
				try {
					BaseNotesTask.this.session = NotesFactory.createSession(
							(String) null, 
							(String) null, 
							(String) BaseNotesTask.this.password
					);
					BaseNotesTask.this.execute(BaseNotesTask.this.session);
				} catch(Throwable e) {
					exHolder.value = e;
				} finally {
					Utils.recycleQuietly(BaseNotesTask.this.session);
				}
			}
		};
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			throw new BuildException(e);
		}
		if( exHolder.value != null ) {
			exHolder.value.printStackTrace(System.err);
			throw new BuildException(exHolder.value);
		}
	}
	
	/**
	 * Returns an opened database object using the current session
	 * @param server the server name
	 * @param database the database name
	 * @return the database
	 * @throws BuildException if the database cannot be opened
	 * @throws NotesException in case of trouble...
	 */
	public Database openDatabase(String server, String database) throws BuildException, NotesException {
		Database ret = this.session.getDatabase(
				server, 
				database, 
				false
		);
		if( ret == null )
			throw new BuildException("Database '" + server + "!!" + database + "' doest not exists");
		if( !ret.isOpen() )
			if( !ret.open() )
				throw new BuildException("Unable to open database '" + server + "!!" + database + "'");
		return ret;
	}
	
	/**
	 * Returns a delegated task
	 * @param cl the class of the object to return
	 * @return the delegated task
	 */
	public <T extends BaseNotesTask> T delegate(Class<T> cl) {
		T ret;
		try {
			ret = cl.newInstance();
		} catch (IllegalAccessException e) {
			throw new BuildException(e);
		} catch (InstantiationException e) {
			throw new BuildException(e);
		}
		ret.setLocation(this.getLocation());
		ret.setOwningTarget(this.getOwningTarget());
		ret.setPassword(this.password);
		ret.setProject(this.getProject());
		ret.setRuntimeConfigurableWrapper(this.getRuntimeConfigurableWrapper());
		ret.setTaskName(this.getTaskName());
		ret.setTaskType(this.getTaskType());
		
		ret.setPassword(this.password);
		
		return ret;
	}
	
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	
	// ========================= GETTERS AND SETTERS ======================================
	
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the session
	 */
	public Session getSession() {
		return session;
	}
}

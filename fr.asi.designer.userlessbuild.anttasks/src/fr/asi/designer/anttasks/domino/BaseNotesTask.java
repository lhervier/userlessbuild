package fr.asi.designer.anttasks.domino;

import java.lang.Thread.UncaughtExceptionHandler;

import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.NotesThread;
import lotus.domino.Session;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import fr.asi.designer.anttasks.util.ExceptionHolder;
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
	 * Execution
	 * @throws NotesException in cas of trouble...
	 */
	public abstract void execute(Session session) throws NotesException;
	
	/**
	 * Execution
	 */
	public final void execute() {
		final ExceptionHolder holder = new ExceptionHolder();
		UncaughtExceptionHandler h = new UncaughtExceptionHandler() {
			public void uncaughtException(Thread th, Throwable ex) {
				holder.ex = ex;
			}
		};
		Thread t = new NotesThread() {
			public void runNotes() {
				Session session = null;
				try {
					session = NotesFactory.createSession(
							(String) null, 
							(String) null, 
							(String) BaseNotesTask.this.password
					);
					BaseNotesTask.this.execute(session);
				} catch(NotesException e) {
					throw new BuildException(e);
				} finally {
					Utils.recycleQuietly(session);
				}
			}
		};
		t.setUncaughtExceptionHandler(h);
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			this.log(holder.ex, Project.MSG_INFO);
		}
		if( holder.ex != null ) {
			throw new BuildException(holder.ex);
		}
	}
	
	// ========================= GETTERS AND SETTERS ======================================
	
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
}

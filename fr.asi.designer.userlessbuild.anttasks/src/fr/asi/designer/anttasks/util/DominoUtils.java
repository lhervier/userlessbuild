package fr.asi.designer.anttasks.util;

import java.lang.Thread.UncaughtExceptionHandler;

import lotus.domino.Base;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.NotesThread;
import lotus.domino.Session;

public class DominoUtils {

	/**
	 * Launch a Runnable in a Notes Thread.
	 * @param r the runnable to launch
	 * @throws InterruptedException 
	 */
	public final static void runInNotesThread(final Runnable r) throws InterruptedException {
		final ExceptionHolder holder = new ExceptionHolder();
		
		UncaughtExceptionHandler h = new UncaughtExceptionHandler() {
			public void uncaughtException(Thread th, Throwable ex) {
				holder.ex = ex;
			}
		};
		Thread t = new NotesThread() {
			public void runNotes() {
				r.run();
			}
		};
		t.setUncaughtExceptionHandler(h);
		t.start();
		t.join();
		if( holder.ex != null )
			throw new RuntimeException(holder.ex);
	}

	/**
	 * Launch a console command
	 * @param server the server to send the console command to
	 * @param command the commande to send
	 * @param password password of the local id file tused to connect to the server
	 * @param failSafe true pour ne pas lever d'exception en cas d'erreur.
	 * @throws ConsoleException en cas d'erreur d'envoi de la commande console.
	 * @throws InterruptedException 
	 */
	public final static String sendConsole(
			final String server, 
			final String command, 
			final String password) throws ConsoleException, InterruptedException {
		final StringHolder result = new StringHolder();
		Runnable r = new Runnable() {
			public void run() {
				try {
					Session session = NotesFactory.createSession(
							(String) null, 
							(String) null, 
							(String) password
					);
					result.s = session.sendConsoleCommand(
							server, 
							command
					);
				} catch(NotesException e) {
					throw new RuntimeException(e);
				}
			}
		};
		try {
			DominoUtils.runInNotesThread(r);
		} catch(Throwable ex) {
			throw new ConsoleException(ex);
		}
		return result.s;
	}
	
	/**
	 * To recycle quietly a domino object
	 * @param o the object to recycle
	 */
	public final static void recycleQuietly(Base o) {
		if( o == null )
			return;
		try {
			o.recycle();
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}
}

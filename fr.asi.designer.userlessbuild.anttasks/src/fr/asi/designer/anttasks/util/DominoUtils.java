package fr.asi.designer.anttasks.util;

import java.lang.Thread.UncaughtExceptionHandler;

import lotus.domino.Base;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.NoteCollection;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.NotesThread;
import lotus.domino.Session;

public class DominoUtils {

	/**
	 * Returns the master template defined for the given database
	 * @param server 
	 * @param db 
	 * @param password password of the local id file
	 * @return le nom du master template
	 * @throws InterruptedException 
	 */
	public final static String getMasterTemplateName(
			final String server, 
			final String db, 
			final String password) throws InterruptedException {
		final StringHolder ret = new StringHolder();
		Runnable r = new Runnable() {
			public void run() {
				try {
					Session session = NotesFactory.createSession(
							(String) null, 
							(String) null, 
							(String) password
					);
					
					Database src = session.getDatabase(server, db, false);
					if( src == null ) {
						ret.s = null;
						return;
					}
					if( !src.isOpen() )
						if( !src.open() )
							throw new RuntimeException("Unable to open database '" + server + "!!" + db + "'");
					
					NoteCollection nc = src.createNoteCollection(false);
					nc.setSelectIcon(true);
					nc.buildCollection();
	
					String noteid = nc.getFirstNoteID();
					Document icon = src.getDocumentByID(noteid);
					String title = icon.getItemValueString("$Title");
					
					int pos = title.indexOf("\n#1");
					if( pos == -1 )
						return;
					ret.s = title.substring(pos + 3);
				} catch(NotesException e) {
					throw new RuntimeException(e);
				}
			}
		};
		runInNotesThread(r);
		return ret.s;
	}
	 
	
	/**
	 * Deletes a database on a domino server
	 * @param server the server that holds the database
	 * @param database the database to delete
	 * @param password password of the local id file
	 * @throws InterruptedException
	 */
	public final static void deleteDatabase(
			final String server, 
			final String database, 
			final String password) throws InterruptedException {
		Runnable r = new Runnable() {
			public void run() {
				try {
					Session session = NotesFactory.createSession(
							(String) null, 
							(String) null, 
							(String) password
					);
					Database db = session.getDatabase(
							server, 
							database, 
							false
					);
					if( db != null ) {
						try {
							db.remove();
						} catch(NotesException e) {
							// May happend...
						}
					}
				} catch(NotesException e) {
					throw new RuntimeException(e);
				}
			}
		};
		DominoUtils.runInNotesThread(r);
	}
	
	/**
	 * Copies a database. Destination MUST NOT exists !
	 * @param srcServer
	 * @param srcDatabase
	 * @param destServer
	 * @param destDatabase
	 * @param password password of the local ID file
	 * @throws InterruptedException 
	 */
	public final static void copyDatabase(
			final String srcServer, 
			final String srcDatabase, 
			final String destServer, 
			final String destDatabase, 
			final String password) throws InterruptedException {
		Runnable r = new Runnable() {
			public void run() {
				try {
					Session session = NotesFactory.createSession(
							(String) null, 
							(String) null, 
							(String) password
					);
					
					Database src = session.getDatabase(srcServer, srcDatabase, false);
					if( src == null )
						throw new RuntimeException("Source database '" + srcServer + "!!" + srcDatabase + "' doest not exists");
					if( !src.isOpen() )
						if( !src.open() )
							throw new RuntimeException("Unable to open source database '" + srcServer + "!!" + srcDatabase + "'");
					
					Database dest = session.getDatabase(destServer, destDatabase, false);
					if( dest != null )
						throw new RuntimeException("Database '" + destServer + "!!" + destDatabase + "' already exists");
					
					// Copie la base source
					dest = src.createCopy(destServer, destDatabase);
					if( !dest.isOpen() )
						if( !dest.open() )
							throw new RuntimeException("Unable to open the database i just copied...");
					
					// Copie les documents de la base Source
					DocumentCollection coll = src.getAllDocuments();
					Document doc = coll.getFirstDocument();
					while( doc != null ) {
						Document copy = doc.copyToDatabase(dest);
						copy.save(true, false);
						copy.recycle();
						
						Document tmp = coll.getNextDocument(doc);
						doc.recycle();
						doc = tmp;
					}
				} catch(NotesException e) {
					throw new RuntimeException(e);
				}
			}
		};
		DominoUtils.runInNotesThread(r);
	}
	
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

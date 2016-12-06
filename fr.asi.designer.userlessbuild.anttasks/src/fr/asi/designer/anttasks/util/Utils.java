package fr.asi.designer.anttasks.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import lotus.domino.Base;
import lotus.domino.NotesException;
import lotus.domino.Stream;

public class Utils {
	
	/**
	 * To detect if a string is empty
	 * @param s the string to test
	 * @return true if the string is empty ("" or null)
	 */
	public final static boolean isEmpty(String s) {
		if( s == null )
			return true;
		if( s.length() == 0 )
			return true;
		return false;
	}
	
	/**
	 * Read the content of a file
	 * Content must be encoded using UTF-8
	 * @param f the file to read
	 * @return the text content of the file
	 * @throws IOException 
	 */
	public final static String readFile(File f) throws IOException {
		StringBuffer sb = new StringBuffer();
		InputStream in = null;
		Reader reader = null;
		try {
			in = new FileInputStream(f);
			reader = new InputStreamReader(in, "UTF-8");
			char[] buffer = new char[4 * 1024];
			int read = reader.read(buffer);
			while( read != -1 ) {
				sb.append(buffer, 0, read);
				read = reader.read(buffer);
			}
		} finally {
			closeQuietly(in);
			closeQuietly(reader);
		}
		return sb.toString();
	}
	
	/**
	 * To remove a folder (or a file) and all of its sub folders
	 * @param root the root folder (or file) to delete
	 */
	public final static void deltree(File root) {
		if( root.isDirectory() )
			for( File child : root.listFiles() )
				deltree(child);
		if( root.exists() )
			if( !root.delete() )
				throw new RuntimeException("Unable to remove '" + root.getAbsolutePath() + "'");
	}
	
	/**
	 * Close a object quietly
	 * @param o the object to close
	 */
	public final static void closeQuietly(Closeable o) {
		if( o == null )
			return;
		try {
			o.close();
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Close a stream quietly
	 * @param o the stream to close
	 */
	public final static void closeQuietly(Stream o) {
		if( o == null )
			return;
		try {
			o.close();
		} catch(Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Create a folder with all the needed parents folders
	 * @param folder the folder to create
	 */
	public final static void createFolder(File folder) {
		if( folder.exists() )
			return;
		Utils.createFolder(folder.getParentFile());
		folder.mkdir();
	}

	
	/**
	 * Create a new file with the given content
	 * @param content the content to set (using UTF-8 encoding)
	 * @return the newly created temporary file
	 * @throws IOException 
	 */
	public final static File createFile(String content) throws IOException {
		File f = File.createTempFile("file", "tmp");
		return Utils.createFile(f, content);
	}

	/**
	 * Create a new file with the given content
	 * @param content the content to set (using UTF-8 encoding)
	 * @return the file
	 * @throws IOException 
	 */
	public final static File createFile(File f, String content) throws IOException {
		// On s'assure que son dossier parent existe
		createFolder(f.getParentFile());
		
		// Vide le fichier
		if( f.exists() ) {
			if( !f.delete() )
				throw new RuntimeException("Unable to remove file '" + f.getAbsolutePath() + "'");
		}
		f.createNewFile();
		
		// Envoi le contenu dans le fichier
		OutputStream out = null;
		Writer writer = null;
		try {
			out = new FileOutputStream(f);
			writer = new OutputStreamWriter(out, "UTF-8");
			writer.write(content);
		} finally {
			Utils.closeQuietly(writer);
			Utils.closeQuietly(out);
		}
		return f;
	}

	/**
	 * Recycle a notes object
	 * @param o the object to recycle
	 */
	public static final void recycleQuietly(Base o) {
		if( o == null )
			return;
		try {
			o.recycle();
		} catch(NotesException e) {
			throw new RuntimeException(e);
		}
	}
}

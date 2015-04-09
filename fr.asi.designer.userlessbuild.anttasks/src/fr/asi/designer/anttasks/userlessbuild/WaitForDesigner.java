package fr.asi.designer.anttasks.userlessbuild;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import fr.asi.designer.anttasks.util.Utils;

/**
 * Ant task to run commands with designer.
 * @author Lionel HERVIER
 */
public class WaitForDesigner extends Task {

	/**
	 * Wait for designer to stop
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public void execute() {
		try {
			int maxTimeout = 200;
			int timeout;
			
			// Le fichier de log. On attend jusqu'à ce qu'il soit créé
			timeout = 0;
			File log = new File(this.getProject().getProperty("basedir") + "/HEADLESS0.log");
			while( isDesignerRunning() && !log.exists() && timeout < maxTimeout ) {
				if( timeout % 5 == 0 )
					this.log("Waiting for Designer to create log file: " + log.getAbsolutePath(), Project.MSG_INFO);
				Thread.sleep(1000);
				timeout++;
			}
			if( timeout == maxTimeout )
				throw new RuntimeException("Unable to detect the log file creation");
			
			// On attend que Designer s'arrête.
			// Si une erreur est levée dans les log, on arrête le script.
			InputStream in = null;
			Reader reader = null;
			BufferedReader br = null;
			try {
				in = new FileInputStream(log);
				reader = new InputStreamReader(in, "UTF-8");
				br = new BufferedReader(reader);
				
				timeout = 0;
				do {
					if( timeout % 5 == 0 )
						this.log("Waiting for Designer to shutdown", Project.MSG_INFO);
					
					String line = br.readLine();
					while( line != null ) {
						this.log(line, Project.MSG_INFO);
						if( line.startsWith("Status ERROR: ") )
							throw new RuntimeException("Error detected in HEADLESS0.log file !");
						line = br.readLine();
					}
					
					if( isDesignerRunning() ) {
						Thread.sleep(1000);
						timeout++;
					}
				} while( isDesignerRunning() && timeout < maxTimeout );
				if( timeout == maxTimeout )
					throw new RuntimeException("Je n'arrive pas à détecter l'arrêt de Designer");
			} finally {
				Utils.closeQuietly(br);
				Utils.closeQuietly(reader);
				Utils.closeQuietly(in);
			}
			
			this.log("waitForDesigner : Designer has stopped", Project.MSG_INFO);
		} catch (InterruptedException e) {
			this.log(e, Project.MSG_ERR);
			throw new RuntimeException(e);
		} catch (IOException e) {
			this.log(e, Project.MSG_ERR);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Is designer running ?
	 * @return true if it is the case. False otherwise
	 */
	public final static boolean isDesignerRunning() {
		InputStream in = null;
		Reader reader = null;
		BufferedReader breader = null;
		boolean ok = false;
		try {
	        Process p = Runtime.getRuntime().exec(System.getenv("windir") + "\\system32\\tasklist.exe");
			
			in = p.getInputStream();
			reader = new InputStreamReader(in);
			breader = new BufferedReader(reader);
	        
			String line;
			while( (line = breader.readLine()) != null ) {
				if( line.indexOf("notes2.exe") != -1 )
					ok = true;
	        }
	    } catch(IOException e) {
			throw new RuntimeException(e);
		} finally {
			Utils.closeQuietly(breader);
			Utils.closeQuietly(reader);
			Utils.closeQuietly(in);
		}
		return ok;
	}
}

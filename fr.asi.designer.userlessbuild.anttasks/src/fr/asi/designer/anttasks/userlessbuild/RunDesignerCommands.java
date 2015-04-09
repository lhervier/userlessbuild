package fr.asi.designer.anttasks.userlessbuild;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.util.FileUtils;

import fr.asi.designer.anttasks.util.Utils;

/**
 * Ant task to run commands with designer.
 * @author Lionel HERVIER
 */
public class RunDesignerCommands extends Task {

	/**
	 * The command text
	 */
	private String commands;
	
	/**
	 * Path to designer
	 */
	private String designerPath;
	
	/**
	 * Set the path to designer
	 * @param designerPath
	 */
	public void setDesignerPath(String designerPath) {
		this.designerPath = designerPath;
	}

	/**
	 * Setter for the command text
	 * @param txt the command text
	 */
	public void addText(String txt) {
		try {
			Reader reader = new StringReader(txt);
			BufferedReader breader = new BufferedReader(reader);
			String line = breader.readLine();
			while( line != null ) {
				this.commands += this.getProject().replaceProperties(line.trim()) + "\n";
				line = breader.readLine();
			}
		} catch (IOException e) {
			this.log(e, Project.MSG_ERR);
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Run the task
	 */
	public void execute() {
		try {
			this.log("Running commands from ant task", Project.MSG_INFO);
			
			this.log("Commands to run : ", Project.MSG_DEBUG);
			this.log(this.commands);
			
			this.log("Removing previous log files", Project.MSG_INFO);
			FileUtils.delete(new File("HEADLESS0.log"));
			FileUtils.delete(new File("HEADLESSJOB0.log"));
			
			File command = Utils.createFile(this.commands);
			Runtime rtm = Runtime.getRuntime();
			Process p = rtm.exec(
					this.designerPath + "designer.exe " + 
					"=" + this.designerPath + "notes.ini " + 
					"-RPARAMS " +
					"-console " + 
					"-vmargs " + 
					"-Dcom.ibm.designer.cmd.file=\"" + command.getAbsolutePath() + "\""
			);
			p.waitFor();
			
			WaitForDesigner w = new WaitForDesigner();
			w.setProject(this.getProject());
			w.setOwningTarget(this.getOwningTarget());
			w.setLocation(this.getLocation());
			w.setTaskName(this.getTaskName());
			w.setTaskType(this.getTaskType());
			w.execute();
			
			Utils.deltree(command);
		} catch (IOException e) {
			this.log(e, Project.MSG_ERR);
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			this.log(e, Project.MSG_ERR);
			throw new RuntimeException(e);
		}
	}
}

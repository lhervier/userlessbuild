package fr.asi.designer.userlessbuild;

import java.util.logging.Logger;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.ibm.designer.domino.tools.userlessbuild.HeadlessLoggerAdapter;

/**
 * Job to check if a given project contains errors.
 * 
 * The job will fail with an error status, so if you included a
 * 		config,true,true
 * in your Designer command file, it will stop the build process.
 * @author Lionel HERVIER
 */
public class CheckErrorJob extends Job {
	
	/**
	 * The logger
	 */
	private Logger logger = HeadlessLoggerAdapter.joblogger;
	
	/**
	 * The project to check
	 */
	private String project;
	
	/**
	 * Constructor
	 * @param project the name of the project to check
	 */
	public CheckErrorJob(String project) {
		super("Checking for error Job");
		this.project = project;
	}

	/**
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStatus run(IProgressMonitor arg0) {
		// Get the project
		logger.finer("CheckErrorJob : Searching the workspace for the project named '" + this.project + "'");
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(this.project);
		if( project == null )
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Unable to find project '" + this.project + "'");
		
		try {
			// Look for markers
			logger.finer("CheckErrorJob : Looking for resources markers");
			IMarker[] markers = project.findMarkers(
					null, 
					true, 
					IResource.DEPTH_INFINITE
			);
			
			// Check markers severity to find errors
			logger.finer("CheckErrorJob : Searching for error markers");
			if( markers != null ) {
				for( IMarker marker : markers ) {
					logger.finer("CheckErrorJob : Analysing marker : ");
					logger.finer("CheckErrorJob :    - id : " + marker.getId());
					Integer severity = (Integer) marker.getAttribute(IMarker.SEVERITY);
					logger.finer("CheckErrorJob :    - severity : " + severity);
					
					if( severity != null && severity.intValue() == IMarker.SEVERITY_ERROR ) {
						logger.finer("CheckErrorJob : Found error marker in project '" + this.project + "'. Existing with error state");
						return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Errors exists in project : ' " + this.project + "'");
					}
			    }
			} else
				logger.finer("CheckErrorJob : No markers found");
			
			// No errors => OK
			logger.finer("CheckErrorJob : No error marker found. Existing with OK state");
			return Status.OK_STATUS;
		} catch(CoreException e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error checking for errors in project '" + this.project + "'", e);
		}
	}
}

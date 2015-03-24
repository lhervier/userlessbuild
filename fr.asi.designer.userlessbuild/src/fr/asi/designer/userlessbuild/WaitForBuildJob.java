package fr.asi.designer.userlessbuild;

import java.util.logging.Logger;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;

import com.ibm.designer.domino.tools.userlessbuild.HeadlessLoggerAdapter;

/**
 * Job pour attendre que les compilations soient terminées
 * @author Lionel HERVIER
 */
public class WaitForBuildJob extends Job {

	/**
	 * Le logger
	 */
	private Logger logger = HeadlessLoggerAdapter.joblogger;
	
	/**
	 * Constructeur
	 */
	public WaitForBuildJob() {
		super("Wait For Build Job");
	}

	/**
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IStatus run(IProgressMonitor arg0) {
		IJobManager jobMan = Job.getJobManager();
		Job[] build = jobMan.find(ResourcesPlugin.FAMILY_AUTO_BUILD); 
		if( build.length == 1 )
			try {
				logger.finer("WaitForBuildJob : Attente de la fin du build auto");
				build[0].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		return Status.OK_STATUS;
	}

}

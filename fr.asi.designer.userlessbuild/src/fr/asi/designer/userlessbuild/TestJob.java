package fr.asi.designer.userlessbuild;

import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.ibm.designer.domino.tools.userlessbuild.HeadlessLoggerAdapter;

public class TestJob extends Job {

	static final Logger logger = HeadlessLoggerAdapter.logger;
	
	private int boucles;
	private int attente;
	
	public TestJob(String sBoucles, String sAttente) {
		super("Job de test");
		this.boucles = Integer.parseInt(sBoucles);
		this.attente = Integer.parseInt(sAttente);
	}

	@Override
	protected IStatus run(IProgressMonitor arg0) {
		System.out.println("Exécution du Job de test");
		for( int i=0; i<this.boucles; i++ ) {
			System.out.println("Exécution de la boucle : " + i);
			System.out.println("Attente de " + this.attente + "ms");
			try {
				Thread.sleep(this.attente);
			} catch (InterruptedException e) {
				return Status.CANCEL_STATUS;
			}
		}
		System.out.println("Fin du Job de test");
		return Status.OK_STATUS;
	}

}

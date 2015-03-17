package fr.asi.designer.userlessbuild;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.core.isite.ISiteFeature;
import org.eclipse.pde.internal.core.isite.ISiteModel;
import org.eclipse.pde.internal.core.site.WorkspaceSiteModel;
import org.eclipse.pde.internal.ui.build.BuildSiteJob;

import com.ibm.designer.domino.tools.userlessbuild.ProjectUtilities;

@SuppressWarnings("restriction")
public class HDBuildSiteJob extends BuildSiteJob {

	private static ISiteModel getModel(String project) {
		System.out.println("Récupération du model: " + project);
		IProject iProject = ProjectUtilities.getProject(project);
		System.out.println("- Projet récupéré: " + iProject);
		IFile siteXml = iProject.getFile("site.xml");
		System.out.println("- site.xml récupéré: " + siteXml);
		ISiteModel site = new WorkspaceSiteModel(siteXml);
		System.out.println("- WorkspaceSiteModel créé: " + site);
		try {
			site.load();
		} catch(CoreException e) {
			e.printStackTrace(System.err);
			throw new RuntimeException(e);
		}
		System.out.println("- Site chargé");
		return site;
	}
	
	private static IFeatureModel[] getFeatures(String project) {
		ISiteFeature[] sFeatures = getModel(project).getSite().getFeatures();
		ArrayList<IFeatureModel> list = new ArrayList<IFeatureModel>();
		for (int i = 0; i < sFeatures.length; i++) {
			ISiteFeature siteFeature = sFeatures[i];
			IFeatureModel model = PDECore.getDefault().getFeatureModelManager().findFeatureModelRelaxed(siteFeature.getId(), siteFeature.getVersion());
			if (model != null)
				list.add(model);
		}
		return (IFeatureModel[])list.toArray(new IFeatureModel[list.size()]);
	}
	
	public HDBuildSiteJob(String project) {
		super(getFeatures(project), getModel(project));
	}
}

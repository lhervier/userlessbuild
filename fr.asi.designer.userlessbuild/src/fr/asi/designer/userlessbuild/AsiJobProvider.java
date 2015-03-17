package fr.asi.designer.userlessbuild;

import com.ibm.designer.domino.tools.userlessbuild.controller.Cmd;
import com.ibm.designer.domino.tools.userlessbuild.controller.JobProvider;

public class AsiJobProvider implements JobProvider {

	public AsiJobProvider() {
		System.out.println("Instanciation de " + AsiJobProvider.class.getName());
	}
	
	public Class<?> getJobClass(Cmd paramCmd) {
		System.out.println(AsiJobProvider.class.getName() + ": Donne la classe pour " + paramCmd.name);
		try {
			return Class.forName(paramCmd.name);
		} catch (ClassNotFoundException e) {
		}
		return null;
	}
}

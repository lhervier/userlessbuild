package fr.asi.designer.anttasks.domino.impl;

import lotus.domino.Document;
import lotus.domino.NotesException;
import fr.asi.designer.anttasks.domino.BaseDesignElementTask;

/**
 * Task to remove the "prohibit design refresh" flag
 * on design elements.
 * @author Lionel HERVIER
 */
public class ClearProhibitDesignRefresh extends BaseDesignElementTask {

	@Override
	protected void execute(Type type, Document designElement) throws NotesException {
		String flags = designElement.getItemValueString("$Flags");
		int pos = flags.indexOf("P");
		if( pos == -1 )
			return;
		
		String server = designElement.getParentDatabase().getServer();
		String db = designElement.getParentDatabase().getFilePath();
		log(server + "!!" + db + ": Removing 'prohibit design refresh' flag from '" + designElement.getItemValueString("$TITLE") + "' (" + designElement.getUniversalID() + " / " + type + ")");
		flags = flags.substring(0, pos) + flags.substring(pos + 1);
		designElement.replaceItemValue("$Flags", flags);
		designElement.save(true, false);
	}
}

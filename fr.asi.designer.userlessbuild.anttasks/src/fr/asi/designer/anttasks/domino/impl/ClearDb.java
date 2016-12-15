package fr.asi.designer.anttasks.domino.impl;

import lotus.domino.Database;
import lotus.domino.DocumentCollection;
import lotus.domino.NotesException;
import lotus.domino.Session;
import fr.asi.designer.anttasks.domino.BaseDatabaseSetTask;
import fr.asi.designer.anttasks.util.Utils;

/**
 * Ant task to clear the content of an update site database
 * @author Lionel HERVIER
 */
public class ClearDb extends BaseDatabaseSetTask {

	/**
	 * The formula
	 */
	private String formula;
	
	@Override
	public void execute(Session session, String server, String database) throws NotesException {
		if( this.formula == null )
			this.log("Clearing all content in database '" + server + "!!" + database + "'");
		else
			this.log("Clearing '" + this.formula + "' in database '" + server + "!!" + database + "'");
		Database db = null;
		DocumentCollection coll = null;
		try {
			db = this.openDatabase(server, database);
			if( Utils.isEmpty(this.formula) )
				coll = db.getAllDocuments();
			else
				coll = db.search(this.formula);
			coll.removeAll(false);
		} finally {
			Utils.recycleQuietly(coll);
			Utils.recycleQuietly(db);
		}
	}
	
	// ===============================================================
	
	/**
	 * @param formula the formula to set
	 */
	public void setFormula(String formula) {
		this.formula = formula;
	}
}

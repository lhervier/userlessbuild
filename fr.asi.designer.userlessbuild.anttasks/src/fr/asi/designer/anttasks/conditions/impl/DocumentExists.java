package fr.asi.designer.anttasks.conditions.impl;

import lotus.domino.Database;
import lotus.domino.DocumentCollection;
import lotus.domino.NotesException;
import lotus.domino.Session;
import fr.asi.designer.anttasks.conditions.BaseServerDatabaseCondition;
import fr.asi.designer.anttasks.util.Utils;

/**
 * Condition to check if a given set of documents exists in 
 * a database
 * @author Lionel HERVIER
 */
public class DocumentExists extends BaseServerDatabaseCondition {

	/**
	 * The formula
	 */
	private String formula;
	
	/**
	 * @see fr.asi.designer.anttasks.conditions.BaseNotesCondition#eval(lotus.domino.Session)
	 */
	@Override
	protected boolean eval(Session session) throws NotesException {
		Database db = null;
		DocumentCollection coll = null;
		try {
			db = this.openDatabase(this.getServer(), this.getDatabase());
			coll = db.search(this.formula);
			return coll.getCount() != 0;
		} finally {
			Utils.recycleQuietly(coll);
			Utils.recycleQuietly(db);
		}
	}
	
	// ===================================================================================

	/**
	 * @param formula the formula to set
	 */
	public void setFormula(String formula) {
		this.formula = formula;
	}
}

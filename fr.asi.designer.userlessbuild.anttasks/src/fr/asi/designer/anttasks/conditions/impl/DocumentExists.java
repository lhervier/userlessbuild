package fr.asi.designer.anttasks.conditions.impl;

import lotus.domino.Database;
import lotus.domino.DocumentCollection;
import lotus.domino.NotesException;
import lotus.domino.Session;
import fr.asi.designer.anttasks.conditions.BaseNotesCondition;
import fr.asi.designer.anttasks.util.Utils;

/**
 * Condition to check if a given set of documents exists in 
 * a database
 * @author Lionel HERVIER
 */
public class DocumentExists extends BaseNotesCondition {

	/**
	 * The server
	 */
	private String server;
	
	/**
	 * The database
	 */
	private String database;
	
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
			db = this.openDatabase(this.server, this.database);
			coll = db.search(this.formula);
			return coll.getCount() != 0;
		} finally {
			Utils.recycleQuietly(coll);
			Utils.recycleQuietly(db);
		}
	}

	/**
	 * @param server the server to set
	 */
	public void setServer(String server) {
		this.server = server;
	}

	/**
	 * @param database the database to set
	 */
	public void setDatabase(String database) {
		this.database = database;
	}

	/**
	 * @param formula the formula to set
	 */
	public void setFormula(String formula) {
		this.formula = formula;
	}

}

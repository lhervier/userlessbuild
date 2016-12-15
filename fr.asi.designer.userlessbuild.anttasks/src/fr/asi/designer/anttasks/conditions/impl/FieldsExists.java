package fr.asi.designer.anttasks.conditions.impl;

import org.apache.tools.ant.BuildException;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.Item;
import lotus.domino.NotesException;
import lotus.domino.Session;
import fr.asi.designer.anttasks.conditions.BaseServerDatabaseCondition;
import fr.asi.designer.anttasks.util.Utils;

/**
 * A condition to check if a given document contains a given field
 * @author Lionel HERVIER
 */
public class FieldsExists extends BaseServerDatabaseCondition {

	/**
	 * The formula. Must select only one document.
	 */
	private String formula;
	
	/**
	 * The field names (separated with coma)
	 */
	private String fields;

	/**
	 * @see fr.asi.designer.anttasks.conditions.BaseNotesCondition#eval(lotus.domino.Session)
	 */
	@Override
	protected boolean eval(Session session) throws NotesException {
		Database db = null;
		DocumentCollection coll = null;
		Document doc = null;
		try {
			db = this.openDatabase(this.getServer(), this.getDatabase());
			coll = db.search(this.formula);
			if( coll.getCount() != 1 )
				throw new BuildException("The formula must select only one document");
			
			String[] tblFields = this.fields.split(",");
			for( int i=0; i<tblFields.length; i++ )
				tblFields[i] = tblFields[i].trim();
			
			doc = coll.getFirstDocument();
			for( String f : tblFields ) {
				if( !doc.hasItem(f) )
					return false;
				Item it = doc.getFirstItem(f);
				if( it.getType() == Item.TEXT && Utils.isEmpty(it.getValueString()) )
					return false;
			}
			return true;
		} finally {
			Utils.recycleQuietly(doc);
			Utils.recycleQuietly(coll);
			Utils.recycleQuietly(db);
		}
	}
	
	// =============================================================================

	/**
	 * @param formula the formula to set
	 */
	public void setFormula(String formula) {
		this.formula = formula;
	}

	/**
	 * @param fields the fields to set
	 */
	public void setFields(String fields) {
		this.fields = fields;
	}
}

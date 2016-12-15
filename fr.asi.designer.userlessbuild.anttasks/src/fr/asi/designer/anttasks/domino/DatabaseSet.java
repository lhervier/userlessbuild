package fr.asi.designer.anttasks.domino;

import java.util.ArrayList;
import java.util.List;

import lotus.domino.Database;
import lotus.domino.DbDirectory;
import lotus.domino.NotesException;
import fr.asi.designer.anttasks.util.Utils;

/**
 * A Set of domino databases
 * @author Lionel HERVIER
 */
public class DatabaseSet {

	/**
	 * the parent task
	 */
	private BaseDatabaseSetTask parentTask;
	
	/**
	 * A template name the databases must inherit from
	 */
	private String template;

	/**
	 * A database name
	 */
	private String database;
	
	/**
	 * Return the databases path
	 * @return the databases path
	 * @throws NotesException
	 */
	public List<String> getPaths() throws NotesException {
		final List<String> ret = new ArrayList<String>();
		
		if( !Utils.isEmpty(this.database) )
			ret.add(this.database);
		
		else if( !Utils.isEmpty(this.template) ) {
			DbDirectory dir = null;
			try {
				dir = this.parentTask.getSession().getDbDirectory(this.parentTask.getServer());
				Database db = dir.getFirstDatabase(DbDirectory.DATABASE);
				while( db != null ) {
					if( db.getDesignTemplateName().equals(this.template) )
						ret.add(db.getFilePath());
					db = dir.getNextDatabase();
				}
			} finally {
				Utils.recycleQuietly(dir);
			}
		}
		return ret;
	}
	
	// ===============================================================================================
	
	/**
	 * @param parentTask the parentTask to set
	 */
	void setParentTask(BaseDatabaseSetTask parentTask) {
		this.parentTask = parentTask;
	}

	/**
	 * @param template the template to set
	 */
	public void setTemplate(String template) {
		this.template = template;
	}
	
	/**
	 * @param database the database to set
	 */
	public void setDatabase(String database) {
		this.database = database;
	}
}

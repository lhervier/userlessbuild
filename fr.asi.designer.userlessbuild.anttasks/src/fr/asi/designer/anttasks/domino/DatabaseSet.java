package fr.asi.designer.anttasks.domino;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import lotus.domino.Database;
import lotus.domino.DbDirectory;
import lotus.domino.NotesException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.condition.Condition;
import org.apache.tools.ant.taskdefs.condition.ConditionBase;

import fr.asi.designer.anttasks.conditions.BaseServerDatabaseCondition;
import fr.asi.designer.anttasks.util.Utils;

/**
 * A Set of domino databases
 * @author Lionel HERVIER
 */
public class DatabaseSet extends ConditionBase {

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
		
		// Extract the list of all the corresponding databases
		if( !Utils.isEmpty(this.database) ) {
			if( this.isSelectedDatabase(this.database) )
				ret.add(this.database);
		
		} else if( !Utils.isEmpty(this.template) ) {
			DbDirectory dir = null;
			try {
				dir = this.parentTask.getSession().getDbDirectory(this.parentTask.getServer());
				Database db = dir.getFirstDatabase(DbDirectory.DATABASE);
				while( db != null ) {
					if( db.getDesignTemplateName().equals(this.template) ) {
						if( this.isSelectedDatabase(db) )
							ret.add(db.getFilePath());
					} 
					db = dir.getNextDatabase();
				}
			} finally {
				Utils.recycleQuietly(dir);
			}
		}
		
		return ret;
	}
	
	/**
	 * Returns true if the selected database must be part
	 * of the database set
	 * @param database the database
	 * @return true or false
	 * @throws NotesException
	 */
	private boolean isSelectedDatabase(String database) throws NotesException {
		Database db = null;
		try {
			db = this.parentTask.openDatabase(this.parentTask.getServer(), database);
			return this.isSelectedDatabase(db);
		} finally {
			Utils.recycleQuietly(db);
		}
	}
	
	/**
	 * Returns true if the selected database must be part
	 * of the database set
	 * @param database the database
	 * @return true or false
	 * @throws NotesException
	 */
	private boolean isSelectedDatabase(Database database) throws NotesException {
		if( this.countConditions() == 0 )
			return true;
		Condition c = this.getCondition();
		this.associate(c, database);		// We need to associate the condition to the current notes context.
		return this.getCondition().eval();
	}
	
	/**
	 * @return the condition
	 */
	private Condition getCondition() {
		return (Condition) this.getConditions().nextElement();
	}
	
	/**
	 * Associate the conditions to the current database
	 * @param condition the condition
	 * @param db the database
	 * @throws NotesException
	 */
	private void associate(Condition condition, Database db) throws NotesException {
		if( condition instanceof BaseServerDatabaseCondition ) {
			BaseServerDatabaseCondition c = (BaseServerDatabaseCondition) condition;
			c.setServer(db.getServer());
			c.setDatabase(db.getFilePath());
			c.setPassword(this.parentTask.getPassword());
		}
		
		if( condition instanceof ConditionBase ) {
			ConditionBase cb = (ConditionBase) condition;
			Enumeration<Condition> en = this.getSubConditions(cb);
			while( en.hasMoreElements() )
				this.associate(en.nextElement(), db);
		}
	}
	
	/**
	 * Return the subconditions of a given condition.
	 * FIXME: Awful hack... getConditions is protected on ConditionBase. So i use reflection to call it...
	 * @param condition the condition
	 * @return the sub conditions
	 */
	@SuppressWarnings("unchecked")
	private Enumeration<Condition> getSubConditions(ConditionBase condition) {
		try {
			Method m = ConditionBase.class.getDeclaredMethod("getConditions", new Class<?>[] {});
			m.setAccessible(true);
			return (Enumeration<Condition>) m.invoke(condition, new Object[] {});
		} catch(Throwable e) {
			throw new BuildException(e);
		}
	}
	
	/**
	 * Returns the associated server
	 */
	public String getServer() {
		return this.parentTask.getServer();
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

package fr.asi.designer.anttasks.domino.impl;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NoteCollection;
import lotus.domino.NotesException;
import lotus.domino.Session;
import fr.asi.designer.anttasks.domino.BaseDatabaseSetTask;
import fr.asi.designer.anttasks.util.Utils;

/**
 * Task to remove the "prohibit design refresh" flag
 * on design elements.
 * @author Lionel HERVIER
 */
public class ClearProhibitDesignRefresh extends BaseDatabaseSetTask {

	/**
	 * Enumeration of all supported design elements
	 */
	private static enum Type {
		ALL,
		ACTIONS,
		AGENTS,
		APPLETS,
		DATABASE_SCRIPTS,
		COLUMNS,
		DATA_CONNECTIONS,
		FILE_RESOURCE,
		HIDDEN_FILE,
		CUSTOM_CONTROLS,
		THEMES,
		XPAGES,
		FOLDERS,
		FORMS,
		FRAMESETS,
		NAVIGATORS,
		OUTLINES,
		PAGES,
		PROFILES,
		SCRIPT_LIBRARIES,
		WEB_SERVICE_CONSUMERS,
		WEB_SERVICE_PROVIDERS,
		SHARED_FIELDS,
		SUBFORMS,
		VIEWS,
		WIRING_PROPERTIES,
		COMPOSITE_APPLICATIONS,
		IMAGES,
		STYLESHEETS,
		DB2_ACCESS_VIEWS,
		ICON
	}
	
	/**
	 * The design elements to select
	 */
	private String select;
	
	/**
	 * Log start message (cosmetic only)
	 */
	private boolean logStart = true;
	
	/**
	 * @see fr.asi.designer.anttasks.domino.BaseDatabaseSetTask#execute(lotus.domino.Session, java.lang.String, java.lang.String)
	 */
	@Override
	protected void execute(Session session, String server, String database) throws NotesException {
		if( logStart )
			this.log("Removing 'prohibit design refresh' flag from '" + this.select + "' design elements of " + server + "!!" + database);
		
		Database db = null;
		NoteCollection nc = null;
		try {
			db = this.openDatabase(server, database);
			nc = db.createNoteCollection(false);
			
			// Selecting all design elements => Delegates to sub tasks
			Type t = Type.valueOf(this.select);
			if( Type.ALL.equals(t) ) {
				for( Type tp : Type.values() ) {
					if( Type.ALL.equals(tp) )
						continue;
					ClearProhibitDesignRefresh task = this.delegate(ClearProhibitDesignRefresh.class);
					task.setSelect(tp.name());
					task.logStart = false;
					task.execute();
				}
				return;
			}
			
			// Build collection
			// See https://www-10.lotus.com/ldd/ddwiki.nsf/dx/ls-design-programming.htm (version 1 !!!!!)
			if( Type.ACTIONS.equals(t) ) {
				nc.setSelectActions(true);
			} else if( Type.AGENTS.equals(t) ) {
				nc.setSelectAgents(true);
			} else if( Type.APPLETS.equals(t) ) {
				nc.selectAllDesignElements(true);
				nc.setSelectionFormula("@Contains($Flags; \"@\")");
			} else if( Type.DATABASE_SCRIPTS.equals(t) ) {
				nc.setSelectDatabaseScript(true);
			} else if( Type.COLUMNS.equals(t) ) {
				nc.setSelectMiscIndexElements(true);
				nc.setSelectionFormula("@Contains($Flags; \"^\")");
			} else if( Type.DATA_CONNECTIONS.equals(t) ) {
				nc.setSelectDataConnections(true);
			} else if( Type.FILE_RESOURCE.equals(t) ) {
				nc.setSelectMiscFormatElements(true);
				nc.setSelectionFormula("@Contains($Flags; \"g\") & !@Matches($Flags; \"*{~K[];`}*\")");
			} else if( Type.HIDDEN_FILE.equals(t) ) {
				nc.setSelectMiscFormatElements(true);
				nc.setSelectionFormula("@Contains($Flags; \"g\") & @Contains($Flags; \"~\") & !@Matches($Flags; \"*{~K[];`}*\")");
			} else if( Type.CUSTOM_CONTROLS.equals(t) ) {
				nc.setSelectMiscFormatElements(true);
				nc.setSelectionFormula("@Contains($Flags; \"g\") & @Contains($Flags; \";\")");
			} else if( Type.THEMES.equals(t) ) {
				nc.setSelectMiscFormatElements(true);
				nc.setSelectionFormula("@Contains($Flags; \"g\") & @Contains($Flags; \"`\")");
			} else if( Type.XPAGES.equals(t) ) {
				nc.setSelectMiscFormatElements(true);
				nc.setSelectionFormula("@Contains($Flags; \"g\") & @Contains($Flags; \"K\")");
			} else if( Type.FOLDERS.equals(t) ) {
				nc.setSelectFolders(true);
			} else if( Type.FORMS.equals(t) ) {
				nc.setSelectForms(true);
			} else if( Type.FRAMESETS.equals(t) ) {
				nc.setSelectFramesets(true);
			} else if( Type.NAVIGATORS.equals(t) ) {
				nc.setSelectNavigators(true);
			} else if( Type.OUTLINES.equals(t) ) {
				nc.setSelectOutlines(true);
			} else if( Type.PAGES.equals(t) ) {
				nc.setSelectPages(true);
			} else if( Type.PROFILES.equals(t) ) {
				nc.setSelectProfiles(true);
			} else if( Type.SCRIPT_LIBRARIES.equals(t) ) {
				nc.setSelectScriptLibraries(true);
				nc.setSelectionFormula("!@Contains($FlagsExt; \"W\")");
			} else if( Type.WEB_SERVICE_CONSUMERS.equals(t) ) {
				nc.setSelectScriptLibraries(true);
				nc.setSelectionFormula("@Contains($FlagsExt; \"W\")");
			} else if( Type.WEB_SERVICE_PROVIDERS.equals(t) ) {
				nc.setSelectMiscCodeElements(true);
				nc.setSelectionFormula("@Contains($Flags; \"{\")");
			} else if( Type.SHARED_FIELDS.equals(t) ) {
				nc.setSelectSharedFields(true);
			} else if( Type.SUBFORMS.equals(t) ) {
				nc.setSelectSubforms(true);
			} else if( Type.VIEWS.equals(t) ) {
				nc.setSelectViews(true);
			} else if( Type.WIRING_PROPERTIES.equals(t) ) {
				nc.setSelectMiscFormatElements(true);
				nc.setSelectionFormula("@Contains($Flags; \":\")");
			} else if( Type.COMPOSITE_APPLICATIONS.equals(t) ) {
				nc.setSelectMiscFormatElements(true);
				nc.setSelectionFormula("@Contains($Flags; \"|\")");
			} else if( Type.IMAGES.equals(t) ) {
				nc.setSelectImageResources(true);
			} else if( Type.STYLESHEETS.equals(t) ) {
				nc.setSelectStylesheetResources(true);
			} else if( Type.DB2_ACCESS_VIEWS.equals(t) ) {
				nc.setSelectMiscFormatElements(true);
				nc.setSelectionFormula("@Contains($Flags; \"z\")");
			} else if( Type.ICON.equals(t) ) {
				nc.setSelectIcon(true);
			} else
				throw new BuildException("Unknown design element type '" + this.select + "'");
			
			nc.buildCollection();
			log("- " + nc.getCount() + " " + this.select + " found", Project.MSG_DEBUG);
			
			String noteId = nc.getFirstNoteID();
			while( !Utils.isEmpty(noteId) ) {
				// log(noteId);
				Document doc = null;
				try {
					doc = db.getDocumentByID(noteId);
					String flags = doc.getItemValueString("$Flags");
					int pos = flags.indexOf("P");
					if( pos != -1 ) {
						log("- Removing flag from " + this.select + " '" + doc.getItemValueString("$TITLE") + "' (" + doc.getUniversalID() + ")");
						flags = flags.substring(0, pos) + flags.substring(pos + 1);
						doc.replaceItemValue("$Flags", flags);
						doc.save(true, false);
					}
				} finally {
					Utils.recycleQuietly(doc);
				}
				
				noteId = nc.getNextNoteID(noteId);
			}
		} finally {
			Utils.recycleQuietly(nc);
			Utils.recycleQuietly(db);
		}
	}
	
	// ======================================================================

	/**
	 * @param select the select to set
	 */
	public void setSelect(String select) {
		this.select = select;
	}
}

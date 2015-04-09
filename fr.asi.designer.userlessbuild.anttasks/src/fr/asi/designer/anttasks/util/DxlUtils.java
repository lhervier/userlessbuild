package fr.asi.designer.anttasks.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class DxlUtils {

	/**
	 * Update the $TITLE file in a DXL file
	 * @param f the DXL file (IconNote or database.properties)
	 * @param title the title to set
	 * @param templateName the templatename to set (null to keep existing)
	 * @param fromTemplate the fromtemplate to set (null to keep existing)
	 * @throws IOException 
	 */
	public final static void updateTitleField(
			File f, 
			String title, 
			String templateName, 
			String fromTemplate) throws IOException {
		String content = Utils.readFile(f);
		
		String dxl = "";
		Reader reader = new StringReader(content);
		BufferedReader breader = new BufferedReader(reader);
		String line = breader.readLine();
		while( line != null ) {
			int pos = line.indexOf("<item name='$TITLE'><text>");
			if( pos != -1 ) {
				dxl += line.substring(0, pos + 26);
				
				// Récupère le titre actuel (de la forme <titre>\n#1<from template>\n#2<template name>
				// Où les #1 et #2 sont facultatifs
				String currTitle = "";
				int posEndText;
				{
					posEndText = line.indexOf("</text>");
					int posStart = pos + 26;
					while( posEndText == -1 ) {
						currTitle += line.substring(posStart) + "\n";
						
						posStart = 0;
						line = breader.readLine();
						if( line == null )
							throw new RuntimeException("Inavlid XML file : " + f.getAbsolutePath());
						posEndText = line.indexOf("</text>");
					}
					currTitle += line.substring(posStart, posEndText);
				}
				
				// On l'interprete pour récupèrer le titre de la base
				if( title == null ) {
					int endPos = currTitle.indexOf('\n');
					title = currTitle.substring(0, endPos);
				}
				
				// On l'interprete pour récupère le nom de template qu'elle défini
				if( templateName == null ) {
					int posTemplateName = currTitle.indexOf("\n#1");
					if( posTemplateName != -1 ) {
						int posEnd = currTitle.indexOf('\n', posTemplateName + 3);
						if( posEnd != -1 )
							templateName = currTitle.substring(posTemplateName + 3, posEnd);
						else
							templateName = currTitle.substring(posTemplateName + 3);
					}
				}
				
				// On l'interprete pour récupère le nom de template dont elle hérite
				if( fromTemplate == null ) {
					int posFromTemplate = currTitle.indexOf("\n#2");
					if( posFromTemplate != -1 ) {
						int posEnd = currTitle.indexOf('\n', posFromTemplate + 3);
						if( posEnd != -1 )
							fromTemplate = currTitle.substring(posFromTemplate + 3, posEnd);
						else
							fromTemplate = currTitle.substring(posFromTemplate + 3);
					}
				}
				
				// Reconstruit la chaîne résultat
				dxl += title + "\n";
				if( templateName != null ) {
					dxl += "#1" + templateName + "\n";
				}
				if( fromTemplate != null ) {
					dxl += "#2" + fromTemplate + "\n";
				}
				
				// Et envoi la fin
				dxl += line.substring(posEndText) + "\n";
			} else 
				dxl += line + "\n";
			
			line = breader.readLine();
		}
		breader.close();
		reader.close();
		
		Utils.createFile(f, dxl);
	}

	/**
	 * Update the title, templatename and fromtemplate attributes 
	 * in the database header of a DXL file
	 * @param f the XML file to update (database.properties)
	 * @param title the title to set (null to keep existing)
	 * @param templateName the templatename to set (null to keep existing)
	 * @param fromTemplate the fromtemplate to set (null to keep existing)
	 * @throws IOException  
	 */
	public final static void updateDbHeader(
			File f, 
			String title, 
			String templateName, 
			String fromTemplate) throws IOException {
		String content = Utils.readFile(f);
		
		String dxl = "";
		Reader reader = new StringReader(content);
		BufferedReader breader = new BufferedReader(reader);
		String line = breader.readLine();
		while( line != null ) {
			int pos = line.indexOf("<database ");
			if( pos != -1 ) {
				dxl += line.substring(0, pos);
				
				// Récupère le tag <database> avec tous ses attributs
				String databaseTag = "";
				int posEndDatabase;
				{
					posEndDatabase = line.indexOf('>', pos);
					int posStart = pos;
					while( posEndDatabase == -1 ) {
						databaseTag += line.substring(posStart) + "\n";
						
						posStart = 0;
						line = breader.readLine();
						if( line == null )
							throw new RuntimeException("Inavlid XML file : " + f.getAbsolutePath());
						posEndDatabase = line.indexOf('>');
					}
					databaseTag += line.substring(posStart, posEndDatabase + 1);
				}
				
				// Met à jour le titre de la base
				if( title != null ) {
					int posTitle = databaseTag.indexOf("title='");
					if( posTitle == -1 )
						throw new RuntimeException("Inavlid DXL file : " + f.getAbsolutePath());
					int posEnd = databaseTag.indexOf('\'', posTitle + 7);
					databaseTag = databaseTag.substring(0, posTitle) + "title='" + title + "'" + databaseTag.substring(posEnd + 1);
				}
				
				// Met à jour le templateName
				if( templateName != null ) {
					int posTemplateName = databaseTag.indexOf("templatename='");
					if( posTemplateName == -1 ) {
						databaseTag = databaseTag.substring(0, databaseTag.length() - 1) + " templatename='" + templateName + "'>";
					} else {
						int posEnd = databaseTag.indexOf('\'', posTemplateName + 14);
						databaseTag = databaseTag.substring(0, posTemplateName) + "templatename='" + templateName + "'" + databaseTag.substring(posEnd + 1);
					}
				}
				
				// Met à jour le fromTemplate
				if( fromTemplate != null ) {
					int posFromTemplate = databaseTag.indexOf("fromtemplate='");
					if( posFromTemplate == -1 ) {
						databaseTag = databaseTag.substring(0, databaseTag.length() - 1) + " fromtemplate='" + fromTemplate + "'>";
					} else {
						int posEnd = databaseTag.indexOf('\'', posFromTemplate + 14);
						databaseTag = databaseTag.substring(0, posFromTemplate) + "fromtemplate='" + fromTemplate + "'" + databaseTag.substring(posEnd + 1);
					}
				}
				
				// Et envoi la fin
				dxl += databaseTag + line.substring(posEndDatabase + 1) + "\n";
			} else 
				dxl += line + "\n";
			
			line = breader.readLine();
		}
		breader.close();
		reader.close();
		
		Utils.createFile(f, dxl);
	}

}

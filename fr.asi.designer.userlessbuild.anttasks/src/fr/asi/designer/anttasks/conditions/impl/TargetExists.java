package fr.asi.designer.anttasks.conditions.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.condition.Condition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.asi.designer.anttasks.util.Utils;

/**
 * This condition check if a given ant file contains a given target
 * @author Lionel HERVIER
 */
public class TargetExists implements Condition {

	/**
	 * Path to the ant file
	 */
	private String antFile;
	
	/**
	 * Name of the target
	 */
	private String target;
	
	/**
	 * The ant project
	 */
	private Project project;
	
	/**
	 * @see org.apache.tools.ant.taskdefs.condition.Condition#eval()
	 */
	@Override
	public boolean eval() throws BuildException {
		InputStream in = null;
		try {
			File buildXml;
			if( this.antFile == null )
				buildXml = new File(this.project.getProperty("basedir") + "/build.xml");
			else
				buildXml = new File(this.project.getProperty("basedir") + "/" + this.antFile);
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = dbf.newDocumentBuilder();
			in = new FileInputStream(buildXml);
			Document document = builder.parse(in, "UTF-8");
			Element root = document.getDocumentElement();
			NodeList targets = root.getElementsByTagName("target");
			for( int i=0; i<targets.getLength(); i++ ) {
				Element target = (Element) targets.item(i);
				if( this.target.equals(target.getAttribute("name")) )
					return true;
			}
			return false;
		} catch (ParserConfigurationException e) {
			throw new BuildException(e);
		} catch (FileNotFoundException e) {
			throw new BuildException(e);
		} catch (SAXException e) {
			throw new BuildException(e);
		} catch (IOException e) {
			throw new BuildException(e);
		} finally {
			Utils.closeQuietly(in);
		}
	}
	
	// ===================================================================================

	/**
	 * @param antFile the antFile to set
	 */
	public void setAntFile(String antFile) {
		this.antFile = antFile;
	}

	/**
	 * @param target the target to set
	 */
	public void setTarget(String target) {
		this.target = target;
	}

	/**
	 * @param project the project to set
	 */
	public void setProject(Project project) {
		this.project = project;
	}

}

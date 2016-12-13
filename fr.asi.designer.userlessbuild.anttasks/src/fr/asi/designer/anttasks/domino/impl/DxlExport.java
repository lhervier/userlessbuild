package fr.asi.designer.anttasks.domino.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import lotus.domino.Database;
import lotus.domino.DxlExporter;
import lotus.domino.NoteCollection;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.Stream;

import org.apache.tools.ant.BuildException;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.asi.designer.anttasks.domino.BaseNotesTask;
import fr.asi.designer.anttasks.util.DOMUtils;
import fr.asi.designer.anttasks.util.Utils;

/**
 * Export a set of documents into a DXL file
 * @author Lionel HERVIER
 */
public class DxlExport extends BaseNotesTask {

	/**
	 * Un log succès
	 */
	public static final String EXPORT_SUCCESS = "<?xml version='1.0'?>\n" +
			"<DXLExporterLog>\n" +
			"</DXLExporterLog>";
	
	/**
	 * Server
	 */
	private String server;
	
	/**
	 * Database
	 */
	private String database;
	
	/**
	 * Formula
	 */
	private String formula;
	
	/**
	 * Destination file
	 */
	private String toFile;
	
	/**
	 * @see fr.asi.designer.anttasks.domino.BaseNotesTask#execute(lotus.domino.Session)
	 */
	@Override
	protected void execute(Session session) throws NotesException {
		this.log("Exporting " + this.formula + " from " + this.server + "!!" + this.database + " to " + this.toFile);
		Database db = null;
		Stream stream = null;
		NoteCollection nc = null;
		DxlExporter exporter = null;
		try {
			// Prepare the stream
			stream = session.createStream();
			File f = new File(this.getProject().getProperty("basedir") + "/" + this.toFile);
			Utils.createFolder(f.getParentFile());		// Ensure folder exists
			if( !stream.open(f.getAbsolutePath()) )
				throw new BuildException("Unable to open file " + toFile + " for writing");
			stream.truncate();
			
			// Build the document collection
			db = this.openDatabase(this.server, this.database);
			nc = db.createNoteCollection(false);
			nc.setSelectDocuments(true);
			nc.setSelectionFormula(this.formula);
			nc.buildCollection();
			
			// Export as a native DXL string
			exporter = session.createDxlExporter();
			exporter.setOutputDOCTYPE(false);
			String dxl = exporter.exportDxl(nc);
			if( !EXPORT_SUCCESS.equals(exporter.getLog()) )
				throw new BuildException("Unable to export DXL: " + exporter.getLog());
			
			// Extract to a DOM object so we can clean it
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = dbf.newDocumentBuilder();
			InputStream in = new ByteArrayInputStream(dxl.getBytes("UTF-8"));
			org.w3c.dom.Document document = builder.parse(in, "UTF-8");
			in.close();
			
			// Clean the XML
			this.clean(document.getDocumentElement());
			
			// Extract as pretty XML string
			OutputFormat format = new OutputFormat(document);
            format.setLineWidth(65);
            format.setIndenting(true);
            format.setIndent(2);
            Writer out = new StringWriter();
            XMLSerializer serializer = new XMLSerializer(out, format);
            serializer.serialize(document);

            String output = out.toString();
            
			stream.writeText(output);
		} catch (ParserConfigurationException e) {
			throw new BuildException(e);
		} catch (UnsupportedEncodingException e) {
			throw new BuildException(e);
		} catch (SAXException e) {
			throw new BuildException(e);
		} catch (IOException e) {
			throw new BuildException(e);
		} catch (TransformerFactoryConfigurationError e) {
			throw new BuildException(e);
		} finally {
			Utils.recycleQuietly(exporter);
			Utils.recycleQuietly(nc);
			Utils.closeQuietly(stream);
			Utils.recycleQuietly(stream);
			Utils.recycleQuietly(db);
		}
	}

	/**
	 * Clean a DXL document so we remove elements that are not mandatory
	 * to re-generate the documents, like replicaid, version, noteinfo, updatedby, wassignedby, etc...
	 * @param rootElt root element
	 */
	private void clean(Element rootElt) {
		// Clean database tag
		DOMUtils.removeAttributes(rootElt, "replicaid", "version", "path", "maintenanceversion");
		DOMUtils.removeChild(rootElt, "databaseinfo");
		
		// Clean documents
		NodeList docLst = rootElt.getElementsByTagName("document");
		for( int i=0; i<docLst.getLength(); i++ ) {
			Element doc = (Element) docLst.item(i);
			DOMUtils.removeChild(doc, "noteinfo", "updatedby");
		}
	}
	
	// ===================================================================================
	
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

	/**
	 * @param toFile the toFile to set
	 */
	public void setToFile(String toFile) {
		this.toFile = toFile;
	}

	
}

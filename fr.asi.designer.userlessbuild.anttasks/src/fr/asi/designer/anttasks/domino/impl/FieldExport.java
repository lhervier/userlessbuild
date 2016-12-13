package fr.asi.designer.anttasks.domino.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.DxlExporter;
import lotus.domino.NoteCollection;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.Stream;

import org.apache.tools.ant.BuildException;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.asi.designer.anttasks.domino.BaseNotesTask;
import fr.asi.designer.anttasks.util.Utils;

/**
 * Export a set of fields from a given document
 * into a dxl fiel
 * @author Lionel HERVIER
 */
public class FieldExport extends BaseNotesTask {

	/**
	 * Un log succès
	 */
	private static final String EXPORT_SUCCESS = "<?xml version='1.0'?>\n" +
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
	 * Name of the fields to extract
	 */
	private String fields;
	
	/**
	 * Destination file
	 */
	private String toFile;
	
	/**
	 * @see fr.asi.designer.anttasks.domino.BaseNotesTask#execute(lotus.domino.Session)
	 */
	@Override
	protected void execute(Session session) throws NotesException {
		this.log(
				"Exporting fields " + this.fields + 
				" from document selected by " + this.formula + 
				" from database " + this.server + "!!" + this.database + 
				" to file " + this.toFile);
		Database db = null;
		Stream stream = null;
		NoteCollection nc = null;
		DxlExporter exporter = null;
		Document doc = null;
		try {
			// Open the stream
			stream = session.createStream();
			File f = new File(this.getProject().getProperty("basedir") + "/" + this.toFile);
			Utils.createFolder(f.getParentFile());		// Ensure parent folder exists
			if( !stream.open(f.getAbsolutePath()) )
				throw new BuildException("Unable to open file " + toFile + " for writing");
			stream.truncate();
			
			// Build the document collection
			db = this.openDatabase(this.server, this.database);
			nc = db.createNoteCollection(false);
			nc.setSelectDocuments(true);
			nc.setSelectionFormula(this.formula);
			nc.buildCollection();
			
			// Check that the selection extract only one document
			if( nc.getCount() == 0 )
				return;
			if( nc.getCount() > 1 )
				throw new BuildException("No documents selected for formula " + this.formula);
			
			// Get the document
			doc = db.getDocumentByID(nc.getFirstNoteID());
			
			// Export the only document as a native DXL string
			exporter = session.createDxlExporter();
			exporter.setOutputDOCTYPE(false);
			String dxl = exporter.exportDxl(doc);
			if( !EXPORT_SUCCESS.equals(exporter.getLog()) )
				throw new BuildException("Unable to export DXL: " + exporter.getLog());
			
			// Extract to a DOM object so we can manipulate it
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = dbf.newDocumentBuilder();
			InputStream in = new ByteArrayInputStream(dxl.getBytes("UTF-8"));
			org.w3c.dom.Document document = builder.parse(in, "UTF-8");
			in.close();
			
			// Clean the DXL
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
			Utils.recycleQuietly(doc);
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
		// Remove all document tag attributes
		NamedNodeMap attrs = rootElt.getAttributes();
		List<Node> toRemove = new ArrayList<Node>();
		for( int i=0; i<attrs.getLength(); i++ ) {
			Node nd = attrs.item(i);
			if( nd.getNodeType() != Node.ATTRIBUTE_NODE )
				continue;
			Attr attr = (Attr) nd;
			if( "xmlns".equals(attr.getName()) )
				continue;
			toRemove.add(attr);
		}
		for( Node nd : toRemove )
			rootElt.removeAttribute(((Attr) nd).getName());
		
		// Field names to extract
		String[] fs = this.fields.split(",");
		List<String> fields = new ArrayList<String>();
		for( String s : fs )
			fields.add(s.trim());
		
		// Remove all nodes that are not selected items
		toRemove = new ArrayList<Node>();
		NodeList nl = rootElt.getChildNodes();
		for( int i=0; i<nl.getLength(); i++ ) {
			Node nd = nl.item(i);
			if( nd.getNodeType() != Node.ELEMENT_NODE )
				continue;
			Element elt = (Element) nd;
			if( !"item".equals(elt.getTagName()) ) {
				toRemove.add(elt);
				continue;
			}
			String name = elt.getAttribute("name");
			if( !fields.contains(name) ) {
				toRemove.add(elt);
				continue;
			}
		}
		for( Node nd : toRemove )
			rootElt.removeChild(nd);
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

	/**
	 * @param fields the fields to set
	 */
	public void setFields(String fields) {
		this.fields = fields;
	}
}

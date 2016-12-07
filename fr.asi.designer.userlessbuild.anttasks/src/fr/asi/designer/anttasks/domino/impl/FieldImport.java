package fr.asi.designer.anttasks.domino.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import lotus.domino.Database;
import lotus.domino.DxlExporter;
import lotus.domino.DxlImporter;
import lotus.domino.NoteCollection;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.Stream;

import org.apache.tools.ant.BuildException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.asi.designer.anttasks.domino.BaseDatabaseSetTask;
import fr.asi.designer.anttasks.util.Utils;

/**
 * Import fields into a set of documents
 * @author Lionel HERVIER
 */
public class FieldImport extends BaseDatabaseSetTask {

	/**
	 * Success log message
	 */
	private static final String IMPORT_SUCCESS = "<?xml version='1.0'?>\n" +
				"<DXLImporterLog>\n" +
				"</DXLImporterLog>";
	
	/**
	 * The formula to select documents to update
	 */
	private String formula;
	
	/**
	 * The file to get the DXL from
	 */
	private String fromFile;
	
	/**
	 * @see fr.asi.designer.anttasks.domino.BaseDatabaseSetTask#execute(lotus.domino.Session, java.lang.String, java.lang.String)
	 */
	@Override
	protected void execute(Session session, String server, String database) throws NotesException {
		this.log("Importing " + this.fromFile + " to " + server + "!!" + database + " documents selected with formula '" + this.formula + "'");
		
		Database db = null;
		NoteCollection nc = null;
		DxlImporter importer = null;
		DxlExporter exporter = null;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			
			// Get a handle on the database
			db = this.openDatabase(server, database);
			
			// Load the definition of the fields to update
			File fromFile = new File(this.getProject().getProperty("basedir") + "/" + this.fromFile);
			Document fieldDoc = dBuilder.parse(fromFile);
			Element fieldRoot = fieldDoc.getDocumentElement();
			
			// Extract the field names and corresponding Nodes to a map (useful later)
			Map<String, Element> fields = new HashMap<String, Element>();
			NodeList fieldsNl = fieldRoot.getChildNodes();
			for( int i=0; i<fieldsNl.getLength(); i++ ) {
				Node n = fieldsNl.item(i);
				if( n.getNodeType() != Node.ELEMENT_NODE )
					continue;
				Element e = (Element) n;
				if( !"item".equals(e.getTagName()) )
					continue;
				fields.put(e.getAttribute("name"), e);
			}
			log("=> Fields to import: " + fields.keySet().toString());
			
			// Extract the documents to update as DXL
			nc = db.createNoteCollection(false);
			nc.setSelectDocuments(true);
			nc.setSelectionFormula(this.formula);
			nc.buildCollection();
			log("=> Will update " + nc.getCount() + " documents");
			
			// Export as a native DXL string
			exporter = session.createDxlExporter();
			exporter.setOutputDOCTYPE(false);
			String dxl = exporter.exportDxl(nc);
			if( !DxlExport.EXPORT_SUCCESS.equals(exporter.getLog()) )
				throw new BuildException("Unable to export DXL: " + exporter.getLog());
			
			// Extract to a DOM object so we can manipulate it
			InputStream in = new ByteArrayInputStream(dxl.getBytes("UTF-8"));
			org.w3c.dom.Document docs = dBuilder.parse(in, "UTF-8");
			Element docsRoot = docs.getDocumentElement();
			in.close();
			
			// Loop on each document
			NodeList docsNl = docsRoot.getChildNodes();
			for( int i=0; i<docsNl.getLength(); i++ ) {
				Node docNode = docsNl.item(i);
				if( docNode.getNodeType() != Node.ELEMENT_NODE )
					continue;
				Element docElt = (Element) docNode;
				if( !"document".equals(docElt.getTagName()) )
					continue;
				
				NodeList noteInfoNl = docElt.getElementsByTagName("noteinfo");
				if( noteInfoNl.getLength() == 1 ) {
					Element noteInfo = (Element) noteInfoNl.item(0);
					log("=> Updating " + noteInfo.getAttribute("unid"));
				}
				
				// Remove every items in our list
				NodeList docFieldsNl = docElt.getElementsByTagName("item");
				List<Element> toRemove = new ArrayList<Element>();
				for( int j=0; j<docFieldsNl.getLength(); j++ ) {
					Element fieldElement = (Element) docFieldsNl.item(j);
					if( !fields.containsKey(fieldElement.getAttribute("name")) )
						continue;
					toRemove.add(fieldElement);
				}
				for( Element n : toRemove )
					docElt.removeChild(n);
				
				// Add our fields to the DXL
				for( Element n : fields.values() ) {
					Node newNode = docs.importNode(n, true);
					docElt.appendChild(newNode);
				}
			}
			
			// Serialize updated DXL into a string
			DOMSource domSource = new DOMSource(docs);
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			StringWriter sw = new StringWriter();
			StreamResult sr = new StreamResult(sw);
			transformer.transform(domSource, sr);
			dxl = sw.toString();
			
			File tmp = File.createTempFile("fieldImport", ".dxl");
			OutputStream out = null;
			Writer writer = null;
			Stream stream = null;
			try {
				// Store the string into a temp file
				out = new FileOutputStream(tmp);
				writer = new OutputStreamWriter(out, "UTF-8");
				writer.write(dxl);
				writer.flush();
				writer.close();
				out.close();
				
				// Create the notes stream
				stream = session.createStream();
				if ( !stream.open(tmp.getAbsolutePath()) || (stream.getBytes() == 0) )
					throw new BuildException("Unable to open file " + tmp.getAbsolutePath());
				
				// Update documents using a DXL import
				importer = session.createDxlImporter();
				importer.setReplaceDbProperties(false);
				importer.setAclImportOption(DxlImporter.DXLIMPORTOPTION_IGNORE);
				importer.setInputValidationOption(DxlImporter.DXLVALIDATIONOPTION_VALIDATE_NEVER);
				importer.setReplicaRequiredForReplaceOrUpdate(true);
				importer.setDesignImportOption(DxlImporter.DXLIMPORTOPTION_IGNORE);
				importer.setDocumentImportOption(DxlImporter.DXLIMPORTOPTION_REPLACE_ELSE_IGNORE);
				
				importer.importDxl(stream, db);
			} finally {
				Utils.closeQuietly(stream);
				Utils.closeQuietly(writer);
				Utils.closeQuietly(out);
				if( !tmp.delete() )
					throw new BuildException("Unable to remove file " + tmp.getAbsolutePath());
			}
			
			String logs = importer.getLog();
			if( !logs.equals(IMPORT_SUCCESS) )
				throw new BuildException(logs);
		} catch (ParserConfigurationException e) {
			throw new BuildException(e);
		} catch (IOException e) {
			throw new BuildException(e);
		} catch (SAXException e) {
			throw new BuildException(e);
		} catch (TransformerConfigurationException e) {
			throw new BuildException(e);
		} catch (TransformerFactoryConfigurationError e) {
			throw new BuildException(e);
		} catch (TransformerException e) {
			throw new BuildException(e);
		} finally {
			Utils.recycleQuietly(importer);
			Utils.recycleQuietly(exporter);
			Utils.recycleQuietly(nc);
			Utils.recycleQuietly(db);
		}
	}
	
	// =========================================================================

	/**
	 * @param fromFile the fromFile to set
	 */
	public void setFromFile(String fromFile) {
		this.fromFile = fromFile;
	}

	/**
	 * @param formula the formula to set
	 */
	public void setFormula(String formula) {
		this.formula = formula;
	}

}

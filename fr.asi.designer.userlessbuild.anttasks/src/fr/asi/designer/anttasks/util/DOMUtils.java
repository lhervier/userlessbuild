package fr.asi.designer.anttasks.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Usefull dom functions
 * @author Lionel HERVIER
 */
public class DOMUtils {

	/**
	 * Remove attributes from a parent node
	 * @param parentNode the parent Node
	 * @param attributes the attributes names to remove
	 */
	public static void removeAttributes(Element parentNode, String... attributes) {
		for( String attr : attributes )
			parentNode.removeAttribute(attr);
	}
	
	/**
	 * Remove child nodes from a parent node
	 * @param parentNode the parent Node
	 * @param childNodes the name of the child nodes to remove
	 */
	public static void removeChild(Element parentNode, String... childNodes) {
		List<String> nodesToRemove = Arrays.asList(childNodes);
		NodeList children = parentNode.getChildNodes();
		List<Node> toRemove = new ArrayList<Node>();
		for( int i=0; i<children.getLength(); i++ ) {
			Node currNode = children.item(i);
			if( nodesToRemove.contains(currNode.getNodeName()) )
				toRemove.add(currNode);
		}
		for( Iterator<Node> it = toRemove.iterator(); it.hasNext(); ) {
			Node nd = it.next();
			parentNode.removeChild(nd);
		}
	}
}

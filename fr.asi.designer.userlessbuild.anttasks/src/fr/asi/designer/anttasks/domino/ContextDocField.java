package fr.asi.designer.anttasks.domino;

/**
 * A field to set in the context document when running an agent
 * @author Lionel HERVIER
 */
public class ContextDocField {

	/**
	 * The field name
	 */
	private String name;
	
	/**
	 * The field value
	 */
	private String value;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
}

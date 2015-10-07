package fr.asi.designer.anttasks.util;

/**
 * Exception levée ne cas d'erreur lors de l'envoi d'une commande console
 * @author Lionel HERVIER
 */
public class ConsoleException extends Exception {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 8356392717120683636L;

	/**
	 * Constructeur
	 */
	public ConsoleException() {
		super();
	}

	/**
	 * Constructeur
	 * @param message le message
	 */
	public ConsoleException(String message) {
		super(message);
	}

	/**
	 * Constructeur
	 * @param message le message
	 * @param cause la cause
	 */
	public ConsoleException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructeur
	 * @param cause la cause
	 */
	public ConsoleException(Throwable cause) {
		super(cause);
	}

}

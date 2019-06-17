package ch.vd.unireg.exception;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class BatchSystemException extends RuntimeException {
	private static final long serialVersionUID = 4102875781314736228L;

	/**
	 * A unique id generated at instanciation
	 */
	private String id;

	/**
	 * @param message un message d'erreur
	 */
	public BatchSystemException(String message) {
		this(message, null);
	}

	/**
	 * @param message un message d'erreur
	 * @param cause   l'exception qui est la cause de celle-ci
	 */
	public BatchSystemException(String message, Throwable cause) {
		super(message, cause);
		this.id = generateId();
	}

	/**
	 * This method overload the RuntimeException.getMessage() to add the id automatically
	 *
	 * @return the exception message with the id
	 */
	public String getMessage() {
		return super.getMessage() + " (id = " + this.id + ")";
	}

	/**
	 * Returns a unique id based on the timestamp and the host name
	 *
	 * @return the id
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Generate a unique id based on the timestamp and the host name
	 *
	 * @return the id
	 */
	private String generateId() {
		String result = null;
		try {
			result = InetAddress.getLocalHost().getHostName() + "_" + System.currentTimeMillis();
		}
		catch (UnknownHostException e) {
			result = "localhost_" + System.currentTimeMillis();
		}
		return result;
	}
}

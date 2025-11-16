package co.edu.unbosque.exception;

public class NotValidPasswordException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NotValidPasswordException() {
		super("La contraseña debe contener al menos 2 mayúsculas, 2 números y 2 caracteres especiales");
	}
}

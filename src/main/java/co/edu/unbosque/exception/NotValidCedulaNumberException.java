package co.edu.unbosque.exception;

public class NotValidCedulaNumberException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NotValidCedulaNumberException() {
		super("\"El número de teléfono debe tener entre 7 y 15 dígitos y no puede contener caracteres especiales.");
	}

}

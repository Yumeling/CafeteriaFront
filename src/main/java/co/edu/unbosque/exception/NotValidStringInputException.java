package co.edu.unbosque.exception;

public class NotValidStringInputException extends Exception {
	private static final long serialVersionUID = 1L;

	public NotValidStringInputException() {
		super("El texto contiene caracteres inválidos o está vacío.");
	}
}
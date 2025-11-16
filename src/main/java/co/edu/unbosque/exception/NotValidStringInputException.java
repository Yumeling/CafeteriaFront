/**
 * Esta la clase Util, en la cual creamos nuestras excepciones propias, para poder limitar los errores que pueda generar el usuario.
 */

package co.edu.unbosque.exception;

/**
 * Excepción lanzada cuando una entrada de cadena no es válida.
 *
 * Esta excepción se lanza cuando una cadena de texto contiene caracteres
 * inválidos o está vacía, indicando que la entrada no cumple con los requisitos
 * esperados.
 */
public class NotValidStringInputException extends Exception {

	/**
	 * Identificador de versión serializable. Esta constante se utiliza para
	 * controlar la versión de la clase durante la serialización. Es importante
	 * mantener este valor constante para garantizar la compatibilidad entre
	 * diferentes versiones de la clase.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor predeterminado que crea una nueva instancia de
	 * NotValidStringInputException.
	 *
	 * Inicializa la excepción con un mensaje predeterminado que indica que la
	 * entrada de texto contiene caracteres inválidos o está vacía.
	 */
	public NotValidStringInputException() {
		super("El texto contiene caracteres inválidos o está vacío.");
	}
}
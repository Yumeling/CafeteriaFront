package co.edu.unbosque.exception;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExceptionChecker {

	public static void notValidPasswordException(String pass) throws NotValidPasswordException {

		if (pass == null || pass.isEmpty()) {
			throw new NotValidPasswordException();
		}
		String longitud = ".{8,}";
		String mayusculas = "(.*[A-Z].*){1,}";
		String numeros = "(.*[0-9].*){1,}";
		String especiales = "(.*[!@#?¿$%^&*()_+].*){1,}";

		if (!pass.matches(longitud) || !pass.matches(mayusculas) || !pass.matches(numeros)
				|| !pass.matches(especiales)) {
			throw new NotValidPasswordException();
		}
	}

	public static void notValidUsernameException(String username) throws NotValidUsernameException {
		if (username == null || username.isEmpty()) {
			throw new NotValidUsernameException("El usuario no puede estar vacío.");
		}
		String invalidChars = "&=<>\"' \n\t-!@#$%^*(){}[];:?,/\\|~.";

		for (char c : invalidChars.toCharArray()) {
			if (username.indexOf(c) != -1) {
				throw new NotValidUsernameException("El nombre de usuario contiene caracteres no válidos.");
			}
		}

		if (username.length() < 3 || username.length() > 20) {
			throw new NotValidUsernameException("El nombre de usuario debe tener entre 3 y 20 caracteres.");
		}
	}

	public static void notValidStringInputException(String txt) throws NotValidStringInputException {
		if (txt == null || txt.isEmpty()) {
			throw new NotValidStringInputException();
		}
		Pattern p = Pattern.compile("[^a-zA-ZñÑ ]");
		Matcher m = p.matcher(txt);

		if (m.find() || txt.trim().isEmpty()) {
			throw new NotValidStringInputException();
		}
	}

	public static void notValidCedulaNumberException(String phoneNumber) throws NotValidCedulaNumberException {
		if (phoneNumber == null || phoneNumber.isEmpty()) {
			throw new NotValidCedulaNumberException();
		}
		String regex = "^[0-9]{7,15}$";

		if (!Pattern.matches(regex, phoneNumber)) {
			throw new NotValidCedulaNumberException();
		}
	}

	public static void notValidStringIntException(String text) throws NotValidStringIntException {
		if (text == null || text.isEmpty()) {
			throw new NotValidStringIntException();
		}
		Pattern p = Pattern.compile("[^a-zA-ZñÑ0-9\\s,\\.\\-#]");
		Matcher m = p.matcher(text);

		if (m.find() || text == null || text.trim().isEmpty()) {
			throw new NotValidStringIntException();
		}
	}

}

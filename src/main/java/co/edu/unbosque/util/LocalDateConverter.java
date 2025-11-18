package co.edu.unbosque.util;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.FacesConverter;
import jakarta.faces.convert.Converter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@FacesConverter("localDateConverter")
public class LocalDateConverter implements Converter<LocalDate> {
	private static final DateTimeFormatter F = DateTimeFormatter.ISO_LOCAL_DATE;

	@Override
	public LocalDate getAsObject(FacesContext context, UIComponent component, String value) {
		if (value == null || value.trim().isEmpty())
			return null;
		try {
			return LocalDate.parse(value, F);
		} catch (Exception ex) {
			return null;
		}
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, LocalDate value) {
		if (value == null)
			return "";
		return value.format(F);
	}
}

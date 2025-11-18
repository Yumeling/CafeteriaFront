package co.edu.unbosque.util;

import java.lang.reflect.Type;
import java.time.LocalDate;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

	@Override
	public JsonElement serialize(LocalDate date, Type typeOfSrc, JsonSerializationContext context) {
		return new JsonPrimitive(date.toString());
	}

	@Override
	public LocalDate deserialize(JsonElement json, Type typeOfT, com.google.gson.JsonDeserializationContext context)
			throws JsonParseException {
		try {
			return LocalDate.parse(json.getAsString());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}

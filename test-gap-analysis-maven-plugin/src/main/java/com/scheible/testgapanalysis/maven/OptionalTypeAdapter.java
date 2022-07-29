package com.scheible.testgapanalysis.maven;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 * Unfortunately Gson has no built-in support for Optional.
 *
 * @author sj
 */
class OptionalTypeAdapter implements JsonDeserializer<Optional<?>>, JsonSerializer<Optional<?>> {

	OptionalTypeAdapter() {
	}

	@Override
	public Optional<?> deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public JsonElement serialize(Optional<?> value, Type type, JsonSerializationContext context) {
		return context.serialize(value.orElse(null));
	}
}

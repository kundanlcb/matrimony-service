package com.cm.matrimony_service.common.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA attribute converter for serializing a List of Strings to a JSON string
 * in the database and deserializing it back to a List of Strings.
 */
@Converter
public class StringListJsonConverter implements AttributeConverter<List<String>, String> {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {
	};

	@Override
	public String convertToDatabaseColumn(List<String> attribute) {
		try {
			return OBJECT_MAPPER.writeValueAsString(attribute == null ? List.of() : attribute);
		}
		catch (JsonProcessingException ex) {
			throw new IllegalArgumentException("Unable to serialize string list", ex);
		}
	}

	@Override
	public List<String> convertToEntityAttribute(String dbData) {
		if (dbData == null || dbData.isBlank()) {
			return new ArrayList<>();
		}
		try {
			return OBJECT_MAPPER.readValue(dbData, STRING_LIST);
		}
		catch (JsonProcessingException ex) {
			throw new IllegalArgumentException("Unable to deserialize string list", ex);
		}
	}
}

package com.locally.locally_backend_engine.decoder.ignore404Error;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        // TODO: Add real encryption logic
        if (attribute == null) return null;
        return attribute; // For now, just return as is
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        // TODO: Add real decryption logic
        if (dbData == null) return null;
        return dbData; // For now, just return as is
    }
}
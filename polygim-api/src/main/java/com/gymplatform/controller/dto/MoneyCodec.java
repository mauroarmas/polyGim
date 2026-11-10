package com.gymplatform.controller.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyCodec {
    private MoneyCodec() { }
    public static BigDecimal parse(String text) {
        if (text == null || !text.matches("^(0|[1-9][0-9]{0,16})\\.[0-9]{2}$"))
            throw new IllegalArgumentException("Importe inválido");
        return new BigDecimal(text).setScale(2, RoundingMode.UNNECESSARY);
    }
    public static class StrictString extends JsonDeserializer<String> {
        @Override public String deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            if (!parser.hasToken(JsonToken.VALUE_STRING)) return (String) context.handleUnexpectedToken(String.class, parser);
            return parser.getText();
        }
    }
}

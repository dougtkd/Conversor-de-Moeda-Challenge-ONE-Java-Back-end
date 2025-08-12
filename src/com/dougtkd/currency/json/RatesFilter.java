package com.dougtkd.currency.json;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class RatesFilter {

    /**
     * Recebe o corpo JSON do /latest e devolve apenas as moedas desejadas.
     * Usa JsonParser/JsonObject (árvore do Gson), conforme o card.
     */
    public static Map<String, BigDecimal> pick(String latestBodyJson, Set<String> codes) {
        JsonObject root = JsonParser.parseString(latestBodyJson).getAsJsonObject();
        JsonObject rates = root.getAsJsonObject("conversion_rates");

        Map<String, BigDecimal> out = new LinkedHashMap<>();
        for (String code : codes) {
            if (rates.has(code)) {
                // getAsBigDecimal não existe; fazemos via getAsString -> BigDecimal
                String v = rates.get(code).getAsNumber().toString();
                out.put(code, new BigDecimal(v));
            }
        }
        return out;
    }
}

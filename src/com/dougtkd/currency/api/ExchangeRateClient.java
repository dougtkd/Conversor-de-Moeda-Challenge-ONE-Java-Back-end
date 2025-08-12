package com.dougtkd.currency.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

public class ExchangeRateClient {
    private final HttpClient http;
    private final Gson gson;
    private final String baseUrl;
    private final String apiKey;

    public ExchangeRateClient() {
        this.http = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        this.gson = new Gson(); // simples e suficiente para este projeto

        // Pode sobrescrever a base via variável EXCHANGE_API_BASE, senão usa o padrão da API v6
        this.baseUrl = System.getenv().getOrDefault("EXCHANGE_API_BASE", "https://v6.exchangerate-api.com/v6");

        this.apiKey  = System.getenv("EXCHANGE_API_KEY");
        if (this.apiKey == null || this.apiKey.isBlank()) {
            throw new IllegalStateException(
                    "EXCHANGE_API_KEY não definida. Defina em Run > Edit Configurations… > Environment variables.");
        }
    }

    // ========== APIs de alto nível (prontas p/ uso) ==========

    /** Últimas taxas para uma moeda base (ex.: USD). */
    public LatestResponse latest(String baseCode) throws IOException, InterruptedException {
        return get("/latest/" + baseCode, Map.of(), RequestOptions.DEFAULT, LatestResponse.class);
    }

    /** Últimas taxas com opções por requisição (timeout/headers). */
    public LatestResponse latest(String baseCode, RequestOptions opts) throws IOException, InterruptedException {
        return get("/latest/" + baseCode, Map.of(), opts, LatestResponse.class);
    }

    /** Versão "raw" que retorna HttpResponse para inspeção (status/headers/corpo). */
    public HttpResponse<String> latestRaw(String baseCode) throws IOException, InterruptedException {
        return getRaw("/latest/" + baseCode, Map.of(), RequestOptions.DEFAULT);
    }

    /** Versão "raw" com RequestOptions. */
    public HttpResponse<String> latestRaw(String baseCode, RequestOptions opts) throws IOException, InterruptedException {
        return getRaw("/latest/" + baseCode, Map.of(), opts);
    }

    /** Taxa de um par (ex.: USD -> BRL). */
    public PairResponse pair(String from, String to) throws IOException, InterruptedException {
        return get("/pair/" + from + "/" + to, Map.of(), RequestOptions.DEFAULT, PairResponse.class);
    }

    /** Taxa de um par com opções por requisição. */
    public PairResponse pair(String from, String to, RequestOptions opts) throws IOException, InterruptedException {
        return get("/pair/" + from + "/" + to, Map.of(), opts, PairResponse.class);
    }

    /** Conversão com valor (a API já retorna o resultado multiplicado). */
    public PairResponse convert(String from, String to, BigDecimal amount) throws IOException, InterruptedException {
        return get("/pair/" + from + "/" + to + "/" + amount.toPlainString(),
                Map.of(), RequestOptions.DEFAULT, PairResponse.class);
    }

    /** Conversão com valor + opções por requisição. */
    public PairResponse convert(String from, String to, BigDecimal amount, RequestOptions opts)
            throws IOException, InterruptedException {
        return get("/pair/" + from + "/" + to + "/" + amount.toPlainString(),
                Map.of(), opts, PairResponse.class);
    }

    // ========== RequestOptions (timeout/headers por chamada) ==========

    public static class RequestOptions {
        public static final RequestOptions DEFAULT = new RequestOptions(Duration.ofSeconds(10), Map.of());

        private final Duration timeout;
        private final Map<String, String> headers;

        public RequestOptions(Duration timeout, Map<String, String> headers) {
            this.timeout = timeout;
            this.headers = headers;
        }

        public Duration getTimeout() { return timeout; }
        public Map<String, String> getHeaders() { return headers; }
    }

    // ========== Helpers de URL/Request ==========

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private URI buildUri(String path, Map<String, String> query) {
        StringBuilder sb = new StringBuilder(baseUrl).append("/").append(apiKey).append(path);
        if (query != null && !query.isEmpty()) {
            sb.append("?");
            sb.append(query.entrySet().stream()
                    .map(e -> enc(e.getKey()) + "=" + enc(e.getValue()))
                    .collect(Collectors.joining("&")));
        }
        return URI.create(sb.toString());
    }

    private HttpRequest buildGet(String path, Map<String, String> query, RequestOptions opts) {
        if (opts == null) opts = RequestOptions.DEFAULT;

        HttpRequest.Builder b = HttpRequest.newBuilder(buildUri(path, query))
                .timeout(opts.getTimeout())
                .header("Accept", "application/json")
                .header("User-Agent", "CurrencyConverter/0.1 (Java)");

        if (opts.getHeaders() != null && !opts.getHeaders().isEmpty()) {
            for (Map.Entry<String, String> e : opts.getHeaders().entrySet()) {
                b.header(e.getKey(), e.getValue());
            }
        }
        return b.GET().build();
    }

    // ========== Execução (raw) e parsing com validação de 'result' ==========

    private HttpResponse<String> getRaw(String path, Map<String, String> query, RequestOptions opts)
            throws IOException, InterruptedException {
        HttpRequest req = buildGet(path, query, (opts == null ? RequestOptions.DEFAULT : opts));
        // Sem validação: retorno cru (útil p/ ver status, headers e corpo)
        return http.send(req, HttpResponse.BodyHandlers.ofString());
    }

    private <T> T get(String path, Map<String, String> query, RequestOptions opts, Class<T> type)
            throws IOException, InterruptedException {
        HttpRequest req = buildGet(path, query, (opts == null ? RequestOptions.DEFAULT : opts));

        HttpResponse<String> res;
        try {
            res = http.send(req, HttpResponse.BodyHandlers.ofString());
        } catch (HttpTimeoutException e) {
            long secs = (opts == null ? RequestOptions.DEFAULT.getTimeout() : opts.getTimeout()).toSeconds();
            throw new IOException("Timeout ao consultar a API (" + secs + "s).", e);
        }

        if (res.statusCode() != 200) {
            throw new IOException("Falha HTTP " + res.statusCode() + ": " + res.body());
        }

        // 1) Valida 'result' com JsonParser (simples e robusto)
        JsonObject root = JsonParser.parseString(res.body()).getAsJsonObject();
        String result = root.has("result") && !root.get("result").isJsonNull()
                ? root.get("result").getAsString()
                : null;
        if (!"success".equalsIgnoreCase(result)) {
            throw new IOException("API retornou erro: " + res.body());
        }

        // 2) Converte para o DTO alvo
        return gson.fromJson(res.body(), type);
    }

    // ========== DTOs (simples) ==========

    /** DTO do /latest */
    public static class LatestResponse {
        public String result;
        public String base_code;
        public Map<String, BigDecimal> conversion_rates; // Gson preenche BigDecimal corretamente
    }

    /** DTO do /pair e /pair/{amount} */
    public static class PairResponse {
        public String result;
        public String base_code;
        public String target_code;
        public BigDecimal conversion_rate;
        public BigDecimal conversion_result; // somente quando usa /pair/{from}/{to}/{amount}
    }
}

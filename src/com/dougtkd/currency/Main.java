package com.dougtkd.currency;

import com.dougtkd.currency.api.ExchangeRateClient;
import com.dougtkd.currency.json.RatesFilter;

import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

public class Main {

    private static final Set<String> ALLOWED = Set.of("ARS", "BOB", "BRL", "CLP", "COP", "USD");

    public static void main(String[] args) {
        try {
            ExchangeRateClient client = new ExchangeRateClient();

            // ====== Fase 6: HttpResponse (status, headers, body) ======
            HttpResponse<String> res = client.latestRaw("USD",
                    new ExchangeRateClient.RequestOptions(Duration.ofSeconds(5), Map.of("X-Debug", "true")));

            System.out.println("=== HttpResponse do /latest/USD ===");
            System.out.println("Status: " + res.statusCode());
            System.out.println("Content-Type: " + res.headers().firstValue("content-type").orElse("<desconhecido>"));
            System.out.println("Body (primeiros 200 chars): " +
                    (res.body().length() > 200 ? res.body().substring(0, 200) + "..." : res.body()));
            System.out.println();

            // ====== Fase 7/8: Gson JsonParser + filtro das 6 moedas ======
            Map<String, BigDecimal> filtered = RatesFilter.pick(res.body(), ALLOWED);
            System.out.println("=== Taxas filtradas (base USD) ===");
            filtered.forEach((k, v) -> System.out.println("USD -> " + k + ": " + v));
            System.out.println();

            // ====== Fase 9: Conversões (menu com 6 opções) ======
            runMenu(client);

        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
        }
    }

    private static void runMenu(ExchangeRateClient client) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== Conversor de Moedas ===");
            System.out.println("Base nas 6 moedas: " + ALLOWED);
            System.out.println("1) USD -> BRL");
            System.out.println("2) BRL -> USD");
            System.out.println("3) USD -> ARS");
            System.out.println("4) ARS -> USD");
            System.out.println("5) BRL -> CLP");
            System.out.println("6) CLP -> BRL");
            System.out.println("0) Sair");
            System.out.print("Escolha: ");
            String opt = sc.nextLine().trim();

            if ("0".equals(opt)) {
                System.out.println("Até mais!");
                break;
            }

            try {
                switch (opt) {
                    case "1" -> doConvert(sc, client, "USD", "BRL");
                    case "2" -> doConvert(sc, client, "BRL", "USD");
                    case "3" -> doConvert(sc, client, "USD", "ARS");
                    case "4" -> doConvert(sc, client, "ARS", "USD");
                    case "5" -> doConvert(sc, client, "BRL", "CLP");
                    case "6" -> doConvert(sc, client, "CLP", "BRL");
                    default -> System.out.println("Opção inválida.");
                }
            } catch (Exception ex) {
                System.out.println("Erro na conversão: " + ex.getMessage());
            }
        }
    }

    private static void doConvert(Scanner sc, ExchangeRateClient client, String from, String to)
            throws Exception {
        System.out.printf("Valor em %s: ", from);
        String raw = sc.nextLine().trim().replace(",", ".");
        BigDecimal amount = new BigDecimal(raw);

        ExchangeRateClient.PairResponse r = client.convert(from, to, amount);
        BigDecimal result = r.conversion_result;

        // 2 casas decimais por padrão
        int scale = 2;
        result = result.setScale(scale, java.math.RoundingMode.HALF_UP);

        System.out.printf("%s %s = %s %s%n", amount.toPlainString(), from, result.toPlainString(), to);
    }
}

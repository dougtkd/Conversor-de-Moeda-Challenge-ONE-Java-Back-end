# Conversor de Moedas — Java

Aplicação de linha de comando que consome a **ExchangeRate-API (v6)** para realizar conversões entre moedas com **taxas em tempo real**.  
O projeto utiliza **Java HttpClient/HttpRequest/HttpResponse** e **Gson** para análise do JSON.  
Faz parte do desafio do Programa ONE.

---

## Sumário
- [Objetivo](#objetivo)
- [Tecnologias & Requisitos](#tecnologias--requisitos)
- [Arquitetura](#arquitetura)
- [Como configurar a API Key](#como-configurar-a-api-key)
- [Como executar](#como-executar)
- [Funcionalidades](#funcionalidades)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Decisões Técnicas](#decisões-técnicas)
- [Erros comuns (Troubleshooting)](#erros-comuns-troubleshooting)
- [Licença](#licença)

---

## Objetivo
- Oferecer **menu interativo (CLI)** com pelo menos **6 opções** de conversão entre moedas.
- Buscar **taxas de câmbio dinâmicas** via API, analisar a **resposta JSON** e filtrar moedas de interesse.
- Demonstrar o uso das classes **HttpClient**, **HttpRequest**, **HttpResponse** e a biblioteca **Gson**.

---

## Tecnologias & Requisitos
- **Java**: JDK **11+** (desenvolvido com JDK 21)
- **Gson**: `2.10.1+` (recomendado `2.11.0`)
- **IntelliJ IDEA** (opcional)
- **Postman** (opcional para testar endpoints)
- **Conta gratuita** na ExchangeRate-API (v6) para obter **API Key**

## Arquitetura
- **CLI (Main)**: interface de linha de comando com menu (6 opções), leitura/validação de entrada e exibição dos resultados.
- **Cliente de API (`ExchangeRateClient`)**: encapsula o acesso à ExchangeRate-API usando `HttpClient`, `HttpRequest` e `HttpResponse` (HTTP/2, redirects, timeouts, headers).
- **JSON & DTOs**: parsing com **Gson**. Uso de `JsonParser/JsonObject` quando precisamos navegar na árvore (filtro) e DTOs (`LatestResponse`, `PairResponse`) para mapear respostas.
- **Filtro de moedas (`RatesFilter`)**: extrai do JSON somente os códigos escolhidos (`ARS`, `BOB`, `BRL`, `CLP`, `COP`, `USD`).
- **Configuração**: chave da API via variável de ambiente `EXCHANGE_API_KEY` (sem expor no repositório).
- **Precisão**: valores monetários com `BigDecimal` (evita imprecisão de ponto flutuante).
- **Erros & Resiliência**: verificação de status HTTP, checagem de `result: "success"` e mensagens claras para timeouts/erros.

---

## Como configurar a API Key

### IntelliJ (recomendado)
1. **Run > Edit Configurations…**
2. Selecione sua configuração **Application** (a que roda o `Main`).
3. Em **Environment variables**, clique em **[…]** > **+**:
    - **Name:** `EXCHANGE_API_KEY`
    - **Value:** *sua_chave_da_ExchangeRate-API*
4. **OK > Apply > OK**

### Terminal (fora da IDE)

**PowerShell (Windows):**
```powershell
$Env:EXCHANGE_API_KEY="SUA_CHAVE"
```

**cmd (Windows):**
```bat
set EXCHANGE_API_KEY=SUA_CHAVE
```

**bash/zsh (Linux/macOS/Git Bash):**
```bash
export EXCHANGE_API_KEY="SUA_CHAVE"
```

**Validação rápida (opcional):**
```bash
curl "https://v6.exchangerate-api.com/v6/SUA_CHAVE/latest/USD"
```

---

## Como executar

### IntelliJ
- **Run > Run '…'** (sua configuração Application).
- O console mostra status do **HttpResponse**, taxas filtradas e o **menu** de conversão.

### Maven (se estiver usando)
```bash
mvn -q compile
mvn -q exec:java -Dexec.mainClass=com.dougtkd.currency.Main
```

---

## Funcionalidades
- **Consulta de taxas:** `GET /latest/{BASE}` (ex.: `USD`).
- **Taxa de par:** `GET /pair/{FROM}/{TO}`.
- **Conversão com valor:** `GET /pair/{FROM}/{TO}/{AMOUNT}`.
- **Filtro das moedas:** `ARS`, `BOB`, `BRL`, `CLP`, `COP`, `USD`.
- **Menu CLI** com 6 opções (exemplo): `USD -> BRL`, `BRL -> USD`, `USD -> ARS`, `ARS -> USD`, `BRL -> CLP`, `CLP -> BRL`.

---

## Estrutura do projeto
```text
src/
 └─ main/
    └─ java/
       └─ com/
          └─ dougtkd/
             └─ currency/
                ├─ Main.java
                ├─ json/
                │  └─ RatesFilter.java
                └─ api/
                   └─ ExchangeRateClient.java
README.md
pom.xml
```

---

## Decisões Técnicas
- **HttpClient/HttpRequest/HttpResponse** nativos (Java 11+).
- **Gson** para parsear JSON (simples e estável).
- **BigDecimal** para evitar imprecisão em valores monetários.
- **Variável de ambiente** para a chave: `EXCHANGE_API_KEY`.

---

## Erros comuns (Troubleshooting)
- **`EXCHANGE_API_KEY não definida`** → configure nas **Environment variables** da run config.
- **HTTP 401 / `invalid-key`** → chave incorreta ou endpoint diferente da v6.
- **Timeout** → internet lenta; aumente o timeout na chamada.
- **`gson` não resolvido** → reimporte o Maven (janela **Maven** > **Reload**) ou adicione o JAR.

---

## Licença
Projeto educacional do Programa ONE. Adapte a licença (ex.: MIT) se necessário.

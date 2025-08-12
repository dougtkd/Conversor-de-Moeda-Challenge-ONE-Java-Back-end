# Conversor de Moedas — Java

Aplicação de linha de comando que consome a **ExchangeRate-API (v6)** para realizar conversões entre moedas com **taxas em tempo real**.  
O projeto utiliza **Java HttpClient/HttpRequest/HttpResponse** e **Gson** para análise do JSON.  
Faz parte do desafio do Programa ONE.

---

## Sumário
- [Objetivo](#objetivo)
- [Tecnologias & Requisitos](#tecnologias--requisitos)
- [Arquitetura e Fases do Desafio](#arquitetura-e-fases-do-desafio)
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



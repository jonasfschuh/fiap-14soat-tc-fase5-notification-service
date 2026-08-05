# fiap-14soat-tc-fase5-notification-service

![Java 21](https://img.shields.io/badge/Java_21-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.4.5-%236DB33F.svg?style=for-the-badge&logo=springboot&logoColor=white)
![Swagger](https://img.shields.io/badge/OpenAPI_3-%2385EA2D.svg?style=for-the-badge&logo=swagger&logoColor=black)
![AWS](https://img.shields.io/badge/AWS-%23FF9900.svg?style=for-the-badge&logo=amazonwebservices&logoColor=white)
![Amazon SES](https://img.shields.io/badge/Amazon_SES_SMTP-232F3E?style=for-the-badge&logo=amazonaws&logoColor=white)
![LocalStack](https://img.shields.io/badge/LocalStack-%23000000.svg?style=for-the-badge&logo=localstack&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)
![Kubernetes](https://img.shields.io/badge/Kubernetes-%23326CE5.svg?style=for-the-badge&logo=kubernetes&logoColor=white)
![New Relic](https://img.shields.io/badge/New_Relic-%231CE783.svg?style=for-the-badge&logo=newrelic&logoColor=white)
![Hexagonal Architecture](https://img.shields.io/badge/Hexagonal-Architecture-7B2D8B?style=for-the-badge)
![DDD](https://img.shields.io/badge/Domain--Driven_Design-430098?style=for-the-badge)
![Event-Driven](https://img.shields.io/badge/Event--Driven-FF6D00?style=for-the-badge)
![BDD](https://img.shields.io/badge/BDD-Cucumber-23D96C?style=for-the-badge&logo=cucumber&logoColor=white)
![Cucumber](https://img.shields.io/badge/Cucumber_7.18-%2323D96C.svg?style=for-the-badge&logo=cucumber&logoColor=white)
![JUnit 5](https://img.shields.io/badge/JUnit_5-%2325A162.svg?style=for-the-badge&logo=junit5&logoColor=white)
![JaCoCo](https://img.shields.io/badge/JaCoCo_%E2%89%A580%25-green?style=for-the-badge)
![Mockito](https://img.shields.io/badge/Mockito_5-%23EE4C2C.svg?style=for-the-badge)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-%232671E5.svg?style=for-the-badge&logo=githubactions&logoColor=white)
![Maven](https://img.shields.io/badge/Apache_Maven-%23C71A36.svg?style=for-the-badge&logo=apachemaven&logoColor=white)

---

## 📑 Sumário

- [👤 Autor](#-autor)
- [📋 Descrição](#-descrição)
- [🏗️ Arquitetura](#️-arquitetura)
- [🛠️ Tecnologias Utilizadas](#️-tecnologias-utilizadas)
- [🔒 Proteção da Branch main](#-proteção-da-branch-main)
- [🚀 Execução Local](#-execução-local)
- [🔐 Variáveis de Ambiente](#-variáveis-de-ambiente)
- [🧪 Testes](#-testes)
- [🎬 Vídeos de Apresentação](#-vídeos-de-apresentação)
- [🔗 Repositórios Relacionados](#-repositórios-relacionados)

---

## 👤 Autor

| Nome                 | E-mail                  | RM        | Discord          | WhatsApp        |
|----------------------|-------------------------|-----------|------------------|-----------------|
| Jonas Fernando Schuh | jonasschuh@hotmail.com  | rm369458  | jonasf.schuh     | 47 9 9960-1396  |

**Grupo:** 2 · FIAP 14SOAT Fase 5 — Hackathon

---

## 📋 Descrição

Este repositório contém o **microserviço Notification Service** da plataforma **FIAP X** — responsável por consumir a fila **Amazon SQS `video-events`**, filtrar os eventos `VIDEO_FAILED` e `VIDEO_PROCESSED`, e enviar notificações por e-mail aos usuários.

A aplicação utiliza **Spring Boot 3.4.5 + Java 21**, arquitetura hexagonal e não possui banco de dados. O envio de e-mails funciona com:

- **MailHog** em ambiente local/Docker (`mailhog:1025`)
- **Amazon SES SMTP** em ambientes AWS (`email-smtp.us-east-1.amazonaws.com:587`)

### Principais funcionalidades

| Funcionalidade | Descrição |
|----------------|-----------|
| **Consumer SQS** | Consome mensagens da fila `video-events` |
| **Filtro de eventos** | Notifica sempre `VIDEO_FAILED`; `VIDEO_PROCESSED` depende de `NOTIFY_ON_PROCESSED` |
| **Notificação SMTP** | Envia e-mails HTML usando `JavaMailSender` |
| **Auth Proxy** | Expõe `POST /auth/login` para delegar autenticação ao `auth-lambda` |
| **Observabilidade** | Actuator, Swagger/OpenAPI e suporte ao New Relic |

### Estrutura de Módulos Maven

```text
fiap-14soat-tc-fase5-notification-service/
├── application/      → Controllers REST, DTOs, exception handlers, testes BDD (Cucumber)
├── domain/           → Modelos, enums, use case, ports e exceções de domínio
├── infrastructure/   → Adapters SMTP/SQS, configurações AWS, Swagger, scheduling
└── report-aggregate/ → Agregador de cobertura JaCoCo
```

---

## 🏗️ Arquitetura

### Arquitetura Hexagonal (Ports & Adapters)

```text
┌────────────────────────────────────────────────────────────┐
│                    Application Layer                        │
│   AuthProxyController  │  GlobalExceptionHandler  │ DTOs   │
└─────────────────────────┬──────────────────────────────────┘
                          │  Input Port
┌─────────────────────────▼──────────────────────────────────┐
│                     Domain Layer                            │
│   VideoNotificationEvent │ NotificationResult              │
│   NotificationEventType  │ SendNotificationUseCase         │
│   SendNotificationInputPort │ NotificationEmailPort        │
└─────────────────────────┬──────────────────────────────────┘
                          │  Output Port
┌─────────────────────────▼──────────────────────────────────┐
│                  Infrastructure Layer                       │
│   SqsVideoEventsConsumerAdapter                             │
│   SmtpEmailAdapter                                          │
│   AwsSqsConfiguration │ SwaggerConfiguration                │
└────────────────────────────────────────────────────────────┘
```

### Fluxo de eventos

```text
[video-processing-service / video-status-service]
                 │
                 │ publica em SQS
                 ▼
        [Queue: video-events]
                 │
                 ▼
 [SqsVideoEventsConsumerAdapter] --polling--> [SendNotificationUseCase]
                 │                               │
                 │                               ├── VIDEO_FAILED ─────► sendFailureEmail()
                 │                               └── VIDEO_PROCESSED ─► sendSuccessEmail() se habilitado
                 ▼
         [SmtpEmailAdapter / JavaMailSender]
                 │
        ┌────────┴────────┐
        ▼                 ▼
    [MailHog]       [Amazon SES SMTP]
```

### Infraestrutura local com Docker Compose

```text
┌────────────────────────────────────────────────────────────┐
│ fiap-network (externa e compartilhada entre serviços)      │
│                                                            │
│  ┌────────────────────┐      ┌──────────────────────────┐  │
│  │ notification-api   │      │ fiap-mailhog             │  │
│  │ :8087              │─────►│ SMTP :1025 / UI :8025    │  │
│  └────────────────────┘      └──────────────────────────┘  │
│              │                                             │
│              └────────► LocalStack externo (:4566)         │
└────────────────────────────────────────────────────────────┘
```

---

## 🛠️ Tecnologias Utilizadas

### Core

| Tecnologia | Versão | Uso |
|------------|--------|-----|
| **Java** | 21 | Linguagem da aplicação |
| **Spring Boot** | 3.4.5 | Framework principal |
| **Spring Web** | 6.x | API REST e auth proxy |
| **Spring Actuator** | 3.4.5 | Health checks e métricas |
| **SpringDoc / OpenAPI** | 2.8.8 | Swagger UI |

### Mensageria

| Tecnologia | Ambiente | Uso |
|------------|----------|-----|
| **Amazon SQS** | AWS | Consumo da fila `video-events` |
| **LocalStack** | Local/Docker | Endpoint SQS local compartilhado |
| **Spring Scheduling** | Todos | Polling controlado da fila |

### Email

| Tecnologia | Ambiente | Uso |
|------------|----------|-----|
| **JavaMailSender** | Todos | Abstração de envio SMTP |
| **MailHog** | Local/Docker | SMTP mock + UI web |
| **Amazon SES SMTP** | AWS | Envio real de e-mails |

### Testes

| Ferramenta | Uso |
|------------|-----|
| **JUnit 5** | Testes unitários |
| **Mockito 5.x** | Mocks de SMTP, SQS e adapters |
| **Cucumber 7.18** | Testes BDD |
| **JaCoCo** | Cobertura mínima de 80% |

### DevOps

| Ferramenta | Uso |
|------------|-----|
| **Docker / Docker Compose** | Execução local |
| **Kubernetes** | Orquestração em produção |
| **GitHub Actions** | Validação de Pull Request |
| **Maven Wrapper** | Build padronizado |
| **New Relic** | Observabilidade APM |

---

## 🔒 Proteção da Branch main

As regras abaixo seguem o padrão dos demais repositórios da stack:

| Regra | Valor |
|---|---|
| **Require a pull request before merging** | ✅ Ativado |
| **Required approvals** | `1` aprovação |
| **Dismiss stale reviews on new commits** | ✅ Ativado |
| **Require status checks to pass** | ✅ Ativado |
| **Require branches to be up to date** | ✅ Ativado |
| **Do not allow bypassing** | ✅ Ativado |

### Status check obrigatório

| Check | Job |
|---|---|
| `build-and-test` | Build Maven + testes + cobertura JaCoCo |

---

## 🚀 Execução Local

### Pré-requisitos

- Java 21+
- Docker Desktop
- Rede Docker compartilhada:

```bash
docker network create fiap-network
```

### Opção A — Stack completa com Docker Compose

> ✅ Recomendado para validar o fluxo do serviço com **MailHog**.

```bash
docker compose up --build -d
```

| Serviço | URL | Descrição |
|---------|-----|-----------|
| **API** | http://localhost:8087 | Notification Service |
| **Swagger UI** | http://localhost:8087/swagger-ui.html | Documentação interativa |
| **Actuator** | http://localhost:8087/actuator | Observabilidade |
| **MailHog UI** | http://localhost:8025 | Visualização dos e-mails |
| **MailHog SMTP** | localhost:1025 | Servidor SMTP mock |

```bash
docker compose down
```

### Opção B — Apenas MailHog + aplicação na IDE

```bash
docker compose up -d mailhog
./mvnw spring-boot:run -pl application -Dspring-boot.run.arguments="--spring.profiles.active=docker"
```

Use as seguintes variáveis no IntelliJ/IDE:

```text
AWS_SQS_ENABLED=true
AWS_ENDPOINT_OVERRIDE=http://localhost:4566
SQS_QUEUE_VIDEO_EVENTS=http://localhost:4566/000000000000/video-events
MAIL_HOST=localhost
MAIL_PORT=1025
NOTIFY_ON_PROCESSED=true
SERVER_PORT=8087
```

### Destaque: MailHog

Durante o desenvolvimento local, todos os e-mails ficam disponíveis em:

- **UI Web:** http://localhost:8025
- **SMTP:** `localhost:1025`

Isso permite validar assunto, corpo HTML e destinatário sem depender de SES.

---

## 🔐 Variáveis de Ambiente

| Variável | Exemplo | Uso |
|----------|---------|-----|
| `SERVER_PORT` | `8087` | Porta HTTP da aplicação |
| `AWS_SQS_ENABLED` | `true` | Habilita consumer SQS |
| `AWS_REGION` | `us-east-1` | Região AWS |
| `AWS_ENDPOINT_OVERRIDE` | `http://localhost:4566` | Endpoint LocalStack |
| `SQS_QUEUE_VIDEO_EVENTS` | `http://localhost:4566/000000000000/video-events` | URL da fila |
| `MAIL_HOST` | `mailhog` / `email-smtp.us-east-1.amazonaws.com` | SMTP |
| `MAIL_PORT` | `1025` / `587` | Porta SMTP |
| `MAIL_USERNAME` | `smtp-user` | Usuário SES |
| `MAIL_PASSWORD` | `smtp-password` | Senha SES |
| `MAIL_FROM` | `noreply@fiapx.com` | Remetente |
| `NOTIFY_ON_PROCESSED` | `true` | Habilita e-mail para `VIDEO_PROCESSED` |
| `AUTH_LAMBDA_URL` | `https://...execute-api...amazonaws.com` | Proxy de autenticação |
| `NEW_RELIC_LICENSE_KEY` | `xxxx` | APM |

---

## 🧪 Testes

### Executar todos os testes

```bash
./mvnw clean test
```

### Executar validação completa com cobertura

```bash
./mvnw clean verify
```

Relatório agregado:

```text
report-aggregate/target/site/jacoco-aggregate/index.html
```

### Executar apenas BDD

```bash
./mvnw test -pl application
```

### Estratégia de testes

| Tipo | Ferramenta | Localização |
|------|------------|-------------|
| Unitários (domain) | JUnit 5 + Mockito | `domain/` |
| Unitários (infra) | JUnit 5 + Mockito | `infrastructure/` |
| BDD | Cucumber | `application/` |

---

## 🎬 Vídeos de Apresentação

| Fase | Link |
|------|------|
| Fase 1 | [Apresentação Tech Challenge 1 — RaceForce](https://youtu.be/EKwE8l4yE1M) |
| Fase 2 | [Apresentação Tech Challenge 2 — RaceForce](https://youtu.be/95ml0-H9Vf4) |
| Fase 3 | [Apresentação Tech Challenge 3 — RaceForce](https://www.youtube.com/watch?v=KB-FC_4zsPE) |
| Fase 4 | [Apresentação Tech Challenge 4 — RaceForce](https://www.youtube.com/watch?v=vR3x4kW0l90) |
| Fase 5 | *(em desenvolvimento)* |

---

## 🔗 Repositórios Relacionados

| Ordem | Repositório | Descrição |
|-------|-------------|-----------|
| 1 | [fiap-14soat-tc-fase5-iac-terraform](https://github.com/jonasfschuh/fiap-14soat-tc-fase5-iac-terraform) | VPC, ECS/EKS, S3, SQS, RDS, Cognito — infraestrutura AWS |
| 2 | [fiap-14soat-tc-fase5-auth-lambda](https://github.com/jonasfschuh/fiap-14soat-tc-fase5-auth-lambda) | Lambda Authorizer + Cognito + API Gateway |
| 3 | [fiap-14soat-tc-fase5-video-upload-service](https://github.com/jonasfschuh/fiap-14soat-tc-fase5-video-upload-service) | Master do ambiente local + upload de vídeos |
| 4 | [fiap-14soat-tc-fase5-video-processing-service](https://github.com/jonasfschuh/fiap-14soat-tc-fase5-video-processing-service) | Processamento de vídeos |
| 5 | [fiap-14soat-tc-fase5-video-status-service](https://github.com/jonasfschuh/fiap-14soat-tc-fase5-video-status-service) | Consulta de status |
| 6 | [fiap-14soat-tc-fase5-video-download-service](https://github.com/jonasfschuh/fiap-14soat-tc-fase5-video-download-service) | Download de artefatos |
| 7 | [fiap-14soat-tc-fase5-notification-service](https://github.com/jonasfschuh/fiap-14soat-tc-fase5-notification-service) | Este repositório — notificações por e-mail |
| 8 | [fiap-14soat-tc-fase5-observability](https://github.com/jonasfschuh/fiap-14soat-tc-fase5-observability) | Prometheus + Grafana + observabilidade |

---

<div align="center">

**🎓 Desenvolvido para o Tech Challenge FIAP 14SOAT — Fase 5 (Hackathon)**

*Projeto Acadêmico — Pós-Graduação em Arquitetura de Software · FIAP 2025/2026*

[⬆ Voltar ao topo](#fiap-14soat-tc-fase5-notification-service)

</div>

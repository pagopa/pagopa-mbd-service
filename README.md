# pagoPA Marca da Bollo Digitale Service

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=pagopa_pagopa-mbd-service&metric=alert_status)](https://sonarcloud.io/dashboard?id=pagopa_pagopa-mbd-service)

Expose APIs that will be used by end user to pay a digital "marca da bollo"

---

## Summary 📖

- [Api Documentation 📖](#api-documentation-)
- [Technology Stack 📚](#technology-stack-)
- [Start Project Locally 🚀](#start-project-locally-)
    * [Run locally with Docker](#run-locally-with-docker)
        + [Prerequisites](#prerequisites)
        + [Run docker container](#run-docker-container)
    * [Running the application in dev mode](#running-the-application-in-dev-mode)
- [Develop Locally 💻](#develop-locally-)
    * [Prerequisites](#prerequisites)
    * [Testing 🧪](#testing-)
        + [Unit test](#unit-test)
        + [Integration test [WIP]](#integration-test-wip)
        + [Performance test [WIP]](#performance-test-wip)
- [Contributors 👥](#contributors-)
    * [Maintainers](#maintainers)

---

## Api Documentation 📖

See
the [OpenApi 3 here](https://editor.swagger.io/?url=https://raw.githubusercontent.com/pagopa/pagopa-mbd-service/main/openapi/openapi.json)

---

## Technology Stack 📚

- Java 17
- Spring Boot
- Spring Web
- Hibernate
- JPA
- OpenFeign
- Lombok

---

## Start Project Locally 🚀

#### Prerequisites

- docker

#### Set environment variables

`docker build -t pagopa-mbd-service .`

`cp .env.example .env`

and replace in `.env` with correct values

#### Run docker container

then type :

`docker run -p 8080:8080 --env-file=./.env pagopa-mbd-service`

## Develop Locally 💻

### Prerequisites

- git
- maven
- jdk-17

### Testing 🧪

#### Unit test

Typing `mvn clean verify`

#### Integration test [WIP]

- Run the application
- Install dependencies: `yarn install`
- Run the test: `yarn test`

#### Performance test [WIP]

---

## Contributors 👥

Made with ❤️ by PagoPa S.p.A.

### Maintainers

See `CODEOWNERS` file
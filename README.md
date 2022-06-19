# Verifiable Credentials HTTP API

An implementation of the [Verifiable Credentials HTTP API](https://w3c-ccg.github.io/vc-api/) using [Iron Verifiable Credentials](https://github.com/filip26/iron-verifiable-credentials), [Titanium JSON-LD](https://github.com/filip26/titanium-json-ld) and [Vert.x](https://vertx.io/).

[![Java 17 CI](https://github.com/filip26/vc-http-api/actions/workflows/java17-build.yml/badge.svg)](https://github.com/filip26/vc-http-api/actions/workflows/java17-build.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)


# Demo

[vc.apicatalog.com/*](vc.apicatalog.com)

# Extra Endpoints

## POST /verify?[domain=]
Verifies verifiable credentials and presentations sent in raw JSON[-LD] format, expanded or compacted.

# Contributing

All PR's welcome!

## Building

Fork and clone the project repository.

```bash
> cd iron-verfiable-credentials
> mvn clean package
```

# Resources
* [Verifiable Credentials HTTP API](https://w3c-ccg.github.io/vc-api/)
* [Ed25519Signature 2020 Interoperability Report](https://w3c-ccg.github.io/di-ed25519-test-suite/)
* [https://github.com/w3c-ccg/vc-api/](https://github.com/w3c-ccg/vc-api/)
* [Verifiable Credentials Use Cases](https://www.w3.org/TR/vc-use-cases/)
* [Verifiable Credentials Data Model v1.1](https://www.w3.org/TR/vc-data-model/)

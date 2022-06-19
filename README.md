# Verifiable Credentials HTTP API

An implementation of the [Verifiable Credentials HTTP API](https://w3c-ccg.github.io/vc-api/) using [Iron Verifiable Credentials](https://github.com/filip26/iron-verifiable-credentials), [Titanium JSON-LD](https://github.com/filip26/titanium-json-ld) and [Vert.x](https://vertx.io/).

# Demo

[vc.apicatalog.com/*](vc.apicatalog.com)

# Extra Endpoints

## POST /verify?[domain=]
Verifies verifiable credentials and presentations sent in raw JSON[-LD] format, expanded or compacted.

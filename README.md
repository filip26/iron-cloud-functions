# Verifiable Credentials HTTP API

An example implementation of the [Verifiable Credentials HTTP API](https://w3c-ccg.github.io/vc-api/) using [Iron Verifiable Credentials](https://github.com/filip26/iron-verifiable-credentials), [Titanium JSON-LD](https://github.com/filip26/titanium-json-ld) and [Vert.x](https://vertx.io/).

[![Java 17 CI](https://github.com/filip26/vc-http-api/actions/workflows/java17-build.yml/badge.svg)](https://github.com/filip26/vc-http-api/actions/workflows/java17-build.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)


## Demo

[https://vc.apicatalog.com/*](https://vc.apicatalog.com)

## Extra Endpoints

### POST /verify?[domain=]
Verifies verifiable credentials and presentations sent in raw JSON[-LD] format, expanded or compacted.

## Contributing

All PR's welcome!

### Setup

#### Variables

##### EDDSA Keys
```bash
> export ED_PUBLIC_KEY=[MULTIBASE]
> export ED_PRIVATE_KEY=[MULTIBASE]
```

##### ECDSA P-256 Keys
```bash
> export P256_PUBLIC_KEY=[MULTIBASE]
> export P256_PRIVATE_KEY=[MULTIBASE]
```

##### ECDSA P-384 Keys
```bash
> export P384_PUBLIC_KEY=[MULTIBASE]
> export P384_PRIVATE_KEY=[MULTIBASE]
```

See [IssuingHandler](https://github.com/filip26/iron-vc-api/blob/33004560eafb913ea812e7883d742acaea6da59f/src/main/java/com/apicatalog/vc/service/issuer/IssuingHandler.java#L32) and [VCApiTest](https://github.com/filip26/iron-vc-api/blob/33004560eafb913ea812e7883d742acaea6da59f/src/test/java/com/apicatalog/vc/service/VcApiTest.java#L40) for an example.

### Building

Fork and clone the project repository.

```bash
> cd iron-vc-api
> mvn clean package
```

### Developing

```bash
> cd iron-vc-api
> chmod +x ./bin/start.sh
> ./bin/start.sh dev
```

### Deployment

1. Setup GAE project and install `gcloud` utility. 
2. Create `.env.yaml` in the project root directory
```yaml
env_variables:
  ED_PUBLIC_KEY: [MULTIBASE]
  ED_PRIVATE_KEY: [MULTIBASE]
  P256_PUBLIC_KEY: [MULTIBASE]
  P256_PRIVATE_KEY: [MULTIBASE]
  P384_PUBLIC_KEY: [MULTIBASE]
  P384_PRIVATE_KEY: [MULTIBASE]
```
3. Edit [src/main/appengine/app.yaml](https://github.com/filip26/iron-vc-api/blob/33004560eafb913ea812e7883d742acaea6da59f/src/main/appengine/app.yaml)
4. Compile and deploy
```bash
> ./bin/deploy.sh
```

## Resources
* [Iron Verifiable Credentials](https://github.com/filip26/iron-verifiable-credentials)
* [Verifiable Credentials HTTP API](https://w3c-ccg.github.io/vc-api/)
* [https://github.com/w3c-ccg/vc-api/](https://github.com/w3c-ccg/vc-api/)

## Sponsors

<a href="https://github.com/digitalbazaar">
  <img src="https://avatars.githubusercontent.com/u/167436?s=200&v=4" width="40" />
</a> 

## Commercial Support
Commercial support is available at filip26@gmail.com

# API tests

The CI-ready Postman Collection v2.1 file is:

`soft-shop-api.postman_collection.json`

Generated Newman reports belong in `reports/` and should not be committed.

Run the complete API suite against the Docker Compose environment:

```powershell
npm ci
npm run test:api
```

The runner resets API-only fixtures, executes Newman, returns a non-zero exit
code on failure, and writes JUnit XML plus HTML reports into `reports/`.

The collection includes UC-linked coverage for UC03 and UC05 through UC15.
After replacing or regenerating the base Postman collection, rebuild those cases with:

```powershell
node tests/api/extend-uc-coverage.mjs
```

Use another deployed endpoint with `API_BASE_URL`, or pass
`--base-url=https://example.test` after `--` in the npm command.

## Four-microservice regression

Run only the complete direct-service API regression against an already running environment:

```powershell
npm run test:api:microservices
```

Run it with an isolated Compose environment:

```powershell
npm run test:api:microservices:compose
```

The generated collection covers user, catalog, trade and interaction APIs, cross-service
order/content flows, and the public gateway routing contract. Reports are written to
`reports/microservices/` in JUnit, HTML and JSON formats, with `failure-summary.md` for CI triage.

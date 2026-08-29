$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
$wrapper = Join-Path $repoRoot "shopping_back\shopping_back\mvnw.cmd"
& $wrapper -f (Join-Path $repoRoot "pom.xml") test `
    -pl services/user-service,services/catalog-service,services/trade-service,services/interaction-service
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

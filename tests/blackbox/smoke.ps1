param(
    [string]$BaseUrl = 'http://127.0.0.1:8080',
    [string]$Username = 'demo',
    [string]$Password = 'demo123'
)

$ErrorActionPreference = 'Stop'
$base = $BaseUrl.TrimEnd('/')

function Assert-ApiOk {
    param(
        [string]$Name,
        [object]$Response
    )

    if ($null -eq $Response -or $Response.code -ne 0) {
        throw "$Name failed: unexpected API result"
    }
    Write-Host "[PASS] $Name"
}

$products = Invoke-RestMethod -Method Get -Uri "$base/api/products" -TimeoutSec 15
Assert-ApiOk -Name 'Product list' -Response $products
if (@($products.data).Count -lt 1) {
    throw 'Product list failed: no product returned'
}

$loginBody = @{
    username = $Username
    password = $Password
} | ConvertTo-Json
$login = Invoke-RestMethod -Method Post -Uri "$base/api/auth/login" `
    -ContentType 'application/json' -Body $loginBody -TimeoutSec 15
Assert-ApiOk -Name 'Login' -Response $login
if ([string]::IsNullOrWhiteSpace($login.data.token)) {
    throw 'Login failed: token is empty'
}

$headers = @{ Authorization = "Bearer $($login.data.token)" }
$buyer = Invoke-RestMethod -Method Get -Uri "$base/api/center/buyer" `
    -Headers $headers -TimeoutSec 15
Assert-ApiOk -Name 'Authenticated buyer center' -Response $buyer

$stores = Invoke-RestMethod -Method Get -Uri "$base/api/stores" -TimeoutSec 15
Assert-ApiOk -Name 'Store list' -Response $stores

Write-Host "Black-box smoke test passed against $base"

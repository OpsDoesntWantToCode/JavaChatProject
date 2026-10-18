[CmdletBinding()]
param(
    [string]$PgBin = '',
    [string]$DbHost = 'localhost',
    [int]$Port = 5432,
    [string]$DbUser = 'postgres'
)
$ErrorActionPreference = 'Stop'
function Resolve-PgTool([string]$Name) {
    if ($PgBin) {
        $toolPath = Join-Path $PgBin ($Name + '.exe')
        if (-not (Test-Path -LiteralPath $toolPath -PathType Leaf)) { throw "Missing PostgreSQL tool: $toolPath" }
        return $toolPath
    }
    return (Get-Command $Name -CommandType Application -ErrorAction Stop).Source
}
$psqlTool = Resolve-PgTool 'psql'
$createdbTool = Resolve-PgTool 'createdb'
$testDatabase = 'java_chat_test_' + [guid]::NewGuid().ToString('N')
$connectionArgs = @('--host', $DbHost, '--port', "$Port", '--username', $DbUser, '--no-password')
# No database supplied by the caller: always CREATE a new uniquely named DB.
# Authentication must already be configured (e.g. pgpass); never echo passwords.
& $createdbTool @connectionArgs '--maintenance-db=postgres' '--template=template0' '--encoding=UTF8' $testDatabase
if ($LASTEXITCODE -ne 0) { throw 'Could not create a NEW test database. No schema/test SQL was run.' }
Write-Host "Created isolated test database: $testDatabase"
Write-Host 'Database is kept for inspection; this runner never drops a database.'
& $psqlTool @connectionArgs '-X' '--set=ON_ERROR_STOP=1' '--dbname' $testDatabase '--file' (Join-Path $PSScriptRoot 'run.sql')
if ($LASTEXITCODE -ne 0) { throw "SQL tests FAILED. Inspect only the new database: $testDatabase" }
Write-Host "PASS: schema/constraint suite on $testDatabase"

# Chay tu goc repository: .\src\admin\run-admin.ps1
# Neu Maven chua trong PATH: truyen -MavenCommand <duong-dan-mvn.cmd>.
[CmdletBinding()]
param([string]$MavenCommand = 'mvn.cmd')

$ErrorActionPreference = 'Stop'
$repositoryRoot = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$mavenExecutable = (Get-Command $MavenCommand -CommandType Application -ErrorAction Stop | Select-Object -First 1).Source
$javaExecutable = (Get-Command java -CommandType Application -ErrorAction Stop | Select-Object -First 1).Source

Push-Location -LiteralPath $repositoryRoot
try {
    # Chi su dung POM hien co; khong thay mainClass cua User.
    & $mavenExecutable '-B' 'compile' 'org.apache.maven.plugins:maven-dependency-plugin:3.11.0:build-classpath' '-DincludeGroupIds=org.openjfx' '-Dmdep.outputFile=target/admin-classpath.txt'
    if ($LASTEXITCODE -ne 0) { throw 'Maven build failed; Admin was not launched.' }

    $classpath = (Get-Content -Raw -LiteralPath 'target/admin-classpath.txt').Trim()
    $javafxModules = @($classpath.Split([IO.Path]::PathSeparator) | Where-Object {
        [IO.Path]::GetFileName($_) -match '^javafx-(base|graphics|controls)-.+-win.jar$'
    })
    if ($javafxModules.Count -ne 3) {
        throw 'Expected JavaFX base/graphics/controls Windows jars from the current POM.'
    }
    $modulePath = $javafxModules -join [IO.Path]::PathSeparator
    & $javaExecutable '--module-path' $modulePath '--add-modules' 'javafx.controls' '-cp' 'target/classes' 'admin.AdminApplication'
    if ($LASTEXITCODE -ne 0) { throw 'Admin JavaFX process failed.' }
} finally {
    Pop-Location
}

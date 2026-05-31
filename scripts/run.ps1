# Run compiler on one .src file with UTF-8 console (avoids garbled Chinese on Windows).
# Usage: .\scripts\run.ps1 samples/sample1_basic.src
#        .\scripts\run.ps1 samples/sample1_basic.src -Brief
#        .\scripts\run.ps1 samples/sample1_basic.src -Phase 1   # only Experiment 1 output
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string]$Source,

    [switch]$Brief,

    [ValidateSet(0, 1, 2, 3)]
    [int]$Phase = 0
)

$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
if ($IsWindows -or $env:OS -match "Windows") {
    try { chcp 65001 | Out-Null } catch { }
}

$ErrorActionPreference = "Stop"
Set-Location (Split-Path $PSScriptRoot -Parent)

$argsLine = $Source
if ($Brief) {
    $argsLine = "$argsLine --brief"
}
if ($Phase -gt 0) {
    $argsLine = "$argsLine --exp$Phase"
}

mvn -q -DskipTests compile
mvn -q exec:java "-Dexec.mainClass=edu.groupname.compiler.app.CompilerApplication" "-Dexec.args=$argsLine"
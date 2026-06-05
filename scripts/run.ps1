# Run compiler on one .src file (UTF-8 console).
# Usage:
#   powershell -ExecutionPolicy Bypass -File .\scripts\run.ps1 samples/sample2_if_else.src
#   powershell -ExecutionPolicy Bypass -File .\scripts\run.ps1 samples/sample2_if_else.src -Phase 3

param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string]$Source,
    [switch]$Brief,
    [switch]$Semantic,
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
if ($Brief) { $argsLine = "$argsLine --brief" }
if ($Phase -gt 0) { $argsLine = "$argsLine --exp$Phase" }
if ($Semantic) { $argsLine = "$argsLine --semantic" }

mvn -q compile -DskipTests
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
mvn -q exec:java "-Dexec.args=$argsLine"

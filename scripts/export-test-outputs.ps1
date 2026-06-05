# Export compiler output to docs/test-output/*.txt (UTF-8).
# Usage: powershell -ExecutionPolicy Bypass -File .\scripts\export-test-outputs.ps1

$ErrorActionPreference = "Stop"
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
if ($IsWindows -or $env:OS -match "Windows") {
    try { chcp 65001 | Out-Null } catch { }
}

$root = Split-Path $PSScriptRoot -Parent
Set-Location $root

$outDir = Join-Path $root "docs\test-output"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

Write-Host "Compiling..."
mvn -q clean compile -DskipTests
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$mainClass = "compiler.app.CompilerApplication"

$samples = @(
    "samples/sample1_basic.src",
    "samples/sample2_if_else.src",
    "samples/sample3_loop.src",
    "samples/sample4_break_error.src",
    "samples/sample5_array.src",
    "samples/sample6_float.src",
    "samples/sample7_bool.src",
    "samples/sample8_lexical_error.src"
)

$utf8NoBom = New-Object System.Text.UTF8Encoding $false

foreach ($sample in $samples) {
    $base = [System.IO.Path]::GetFileNameWithoutExtension($sample)
    $target = Join-Path $outDir "$base.txt"

    $mvnOutput = & mvn -q exec:java "-Dexec.args=$sample" 2>&1
    $exitCode = $LASTEXITCODE
    $text = $mvnOutput | Out-String
    if ($exitCode -ne 0) {
        Write-Host $text
        Write-Error "Failed on $sample (exit $exitCode). Hint: run 'mvn clean compile' if ClassNotFoundException."
    }
    [System.IO.File]::WriteAllText($target, $text, $utf8NoBom)
    Write-Host "Wrote $target"
}

Write-Host "Done. Output directory: $outDir"

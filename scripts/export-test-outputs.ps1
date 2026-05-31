# Export compiler output for each sample into docs/test-output/*.txt (for report screenshots).
$ErrorActionPreference = "Stop"
Set-Location (Split-Path $PSScriptRoot -Parent)
$outDir = Join-Path (Get-Location) "docs\test-output"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null
mvn -q -DskipTests compile

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

foreach ($sample in $samples) {
    $base = [System.IO.Path]::GetFileNameWithoutExtension($sample)
    $target = Join-Path $outDir "$base.txt"
    mvn -q exec:java "-Dexec.mainClass=edu.groupname.compiler.app.CompilerApplication" "-Dexec.args=$sample" *> $target
    Write-Host "Wrote $target"
}

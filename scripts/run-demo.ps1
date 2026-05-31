# UTF-8 console output (Windows PowerShell 5 may misread .ps1 without BOM).
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
if ($IsWindows -or $env:OS -match "Windows") {
    try { chcp 65001 | Out-Null } catch { }
}

function U8([byte[]]$Bytes) {
    return [System.Text.Encoding]::UTF8.GetString($Bytes)
}

$LabelSample = U8 @(0xE6, 0xA0, 0xB7, 0xE4, 0xBE, 0x8B)           # 样例
$LabelErrorSample = U8 @(0xE9, 0x94, 0x99, 0xE8, 0xAF, 0xAF, 0xE6, 0xA0, 0xB7, 0xE4, 0xBE, 0x8B)  # 错误样例
$LabelBreakError = U8 @(0x62, 0x72, 0x65, 0x61, 0x6B, 0x20, 0xE4, 0xB8, 0x8D, 0xE5, 0x9C, 0xA8, 0xE5, 0xBE, 0xAA, 0xE7, 0x8E, 0xAF, 0xE5, 0x86, 0x85)  # break 不在循环内

$ErrorActionPreference = "Stop"
Set-Location (Split-Path $PSScriptRoot -Parent)

mvn -q -DskipTests compile

$samples = @(
    "samples/sample1_basic.src",
    "samples/sample2_if_else.src",
    "samples/sample3_loop.src",
    "samples/sample5_array.src",
    "samples/sample6_float.src",
    "samples/sample7_bool.src"
)

foreach ($sample in $samples) {
    Write-Host ""
    Write-Host "################################################################"
    Write-Host "# ${LabelSample}: $sample"
    Write-Host "################################################################"
    mvn -q exec:java "-Dexec.mainClass=edu.groupname.compiler.app.CompilerApplication" "-Dexec.args=$sample"
}

Write-Host ""
Write-Host "################################################################"
Write-Host "# ${LabelErrorSample}: $LabelBreakError"
Write-Host "################################################################"
mvn -q exec:java "-Dexec.mainClass=edu.groupname.compiler.app.CompilerApplication" "-Dexec.args=samples/sample4_break_error.src"

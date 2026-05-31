# Optional: same as run-demo.ps1 but truncates LR(1) table to 30 rows (--brief).
$ErrorActionPreference = "Stop"
Set-Location (Split-Path $PSScriptRoot -Parent)
mvn -q -DskipTests compile
$all = @(
    "samples/sample1_basic.src",
    "samples/sample2_if_else.src"
)
foreach ($sample in $all) {
    mvn -q exec:java "-Dexec.mainClass=edu.groupname.compiler.app.CompilerApplication" "-Dexec.args=$sample --brief"
}

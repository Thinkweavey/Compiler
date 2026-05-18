$ErrorActionPreference = "Stop"
Set-Location (Split-Path $PSScriptRoot -Parent)

mvn -q -DskipTests compile
mvn -q exec:java "-Dexec.mainClass=edu.groupname.compiler.app.CompilerApplication" "-Dexec.args=samples/sample1_basic.src --brief"

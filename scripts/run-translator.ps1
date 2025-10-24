param(
  [Parameter(Mandatory=$true, Position=0)]
  [string]$File
)

$ErrorActionPreference = 'Stop'

# Build
mvn -q -DskipTests compile

# Build classpath for compile scope
mvn -q -DincludeScope=compile dependency:build-classpath -Dmdep.outputFile=cp.txt
$cp = Get-Content cp.txt -Raw
$cp = "$cp;target/classes"

# Run translator
java -cp "$cp" translator.Translator "$File"


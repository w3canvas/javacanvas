#!/usr/bin/env pwsh
# PowerShell wrapper for Gradle that uses project-local .gradle directory
# This avoids Windows username issues (e.g., special characters like apostrophes)

if (-not $env:GRADLE_USER_HOME) {
    $projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
    $env:GRADLE_USER_HOME = Join-Path $projectDir ".gradle"
}

& "$PSScriptRoot/gradlew.bat" @args

#!/usr/bin/env pwsh
# PowerShell wrapper for Gradle that uses the system's shared GRADLE_USER_HOME
# Only use this if you know your username doesn't contain special characters
# Default behavior uses project-local .gradle - use gradlew.ps1 or gradlew.bat instead

# Explicitly unset to use default system location
$env:GRADLE_USER_HOME = $null

& "$PSScriptRoot/gradlew.bat" @args

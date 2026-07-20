# ─────────────────────────────────────────────────────────────────────────────
# export-spec.ps1 — Export the live OpenAPI spec from a running proxy instance.
#
# Usage:
#   .\openapi\export-spec.ps1 [-BaseUrl http://localhost:8080]
#
# The proxy must be running.
# ─────────────────────────────────────────────────────────────────────────────
param(
    [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$Output = Join-Path $ScriptDir "spec.yaml"

Write-Host "Fetching OpenAPI spec from $BaseUrl/v3/api-docs.yaml ..."
Invoke-RestMethod -Uri "$BaseUrl/v3/api-docs.yaml" -OutFile $Output

Write-Host "Spec exported to: $Output"
Write-Host "Commit this file to keep the spec in sync with the proxy."

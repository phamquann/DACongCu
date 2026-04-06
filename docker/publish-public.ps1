param(
  [Parameter(Mandatory = $true)]
  [string]$Namespace,

  [string]$Registry = "docker.io",
  [string]$ProjectName = "dacongcu",
  [string]$VersionTag = "v1.0.0",
  [switch]$PushLatest,
  [switch]$IncludeShaTag,
  [switch]$Login,
  [switch]$NoCache
)

$ErrorActionPreference = "Stop"

function Invoke-Docker {
  param(
    [Parameter(Mandatory = $true)]
    [string[]]$Args
  )

  Write-Host ("`n> docker {0}" -f ($Args -join " ")) -ForegroundColor Cyan
  & docker @Args
  if ($LASTEXITCODE -ne 0) {
    throw "Docker command failed: docker $($Args -join ' ')"
  }
}

if ($Login) {
  Invoke-Docker -Args @("login", $Registry)
}

$backendImage = "$Registry/$Namespace/$ProjectName-backend"
$frontendImage = "$Registry/$Namespace/$ProjectName-frontend"

$tags = New-Object System.Collections.Generic.List[string]
$tags.Add($VersionTag)

if ($PushLatest) {
  $tags.Add("latest")
}

if ($IncludeShaTag) {
  $gitSha = (& git rev-parse --short HEAD 2>$null).Trim()
  if ($gitSha) {
    $tags.Add("sha-$gitSha")
  }
}

$primaryTag = $tags[0]

$buildArgs = @()
if ($NoCache) {
  $buildArgs += "--no-cache"
}

Invoke-Docker -Args (@("build", "-f", "docker/backend.Dockerfile", "-t", "$backendImage`:$primaryTag") + $buildArgs + @("."))
Invoke-Docker -Args (@("build", "-f", "docker/frontend.Dockerfile", "-t", "$frontendImage`:$primaryTag") + $buildArgs + @("."))

foreach ($tag in $tags) {
  if ($tag -ne $primaryTag) {
    Invoke-Docker -Args @("tag", "$backendImage`:$primaryTag", "$backendImage`:$tag")
    Invoke-Docker -Args @("tag", "$frontendImage`:$primaryTag", "$frontendImage`:$tag")
  }
}

foreach ($tag in $tags) {
  Invoke-Docker -Args @("push", "$backendImage`:$tag")
  Invoke-Docker -Args @("push", "$frontendImage`:$tag")
}

Write-Host "`nPublished images:" -ForegroundColor Green
foreach ($tag in $tags) {
  Write-Host "- $backendImage`:$tag"
  Write-Host "- $frontendImage`:$tag"
}
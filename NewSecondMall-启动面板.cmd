@echo off
setlocal EnableExtensions

set "ROOT=%~dp0"
set "PANEL=%ROOT%NewSecondMall-LaunchPanel.ps1"

if not exist "%PANEL%" (
  echo NewSecondMall-LaunchPanel.ps1 was not found.
  echo Please keep this cmd file in the NewSecondMall project root.
  pause
  exit /b 1
)

powershell -NoProfile -ExecutionPolicy Bypass -File "%PANEL%" %*
exit /b %ERRORLEVEL%

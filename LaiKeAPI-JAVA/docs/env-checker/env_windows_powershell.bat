@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
set "PS_SCRIPT=%SCRIPT_DIR%env_windows_powershell.ps1"

if not exist "%PS_SCRIPT%" (
    echo [ERROR] Cannot find script: "%PS_SCRIPT%"
    exit /b 1
)

powershell -NoProfile -ExecutionPolicy Bypass -File "%PS_SCRIPT%"
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] PowerShell checker failed. Please run manually:
    echo powershell -NoProfile -ExecutionPolicy Bypass -File "%PS_SCRIPT%"
    exit /b %errorlevel%
)

pause

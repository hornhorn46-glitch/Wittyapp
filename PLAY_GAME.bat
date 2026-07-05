@echo off
setlocal

set "ROOT=%~dp0"
set "GODOT=%ROOT%tools\godot\Godot_v4.2.2-stable_win64.exe"
cd /d "%ROOT%"

if not exist "%GODOT%" (
    echo Godot executable was not found:
    echo %GODOT%
    echo.
    echo Please make sure the tools\godot folder exists.
    pause
    exit /b 1
)

echo Starting Silent Exit...
echo If the game window opens behind this console, switch to it with Alt+Tab.
echo.

"%GODOT%" --path "."
set "EXIT_CODE=%ERRORLEVEL%"

echo.
echo Silent Exit closed. Exit code: %EXIT_CODE%
pause
exit /b %EXIT_CODE%

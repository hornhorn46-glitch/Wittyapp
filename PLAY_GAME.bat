@echo off
setlocal

set "ROOT=%~dp0"
set "GODOT=%ROOT%tools\godot\Godot_v4.2.2-stable_win64.exe"
set "GODOT_DIR=%ROOT%tools\godot"
set "GODOT_ZIP=%GODOT_DIR%\godot.zip"
set "GODOT_URL=https://github.com/godotengine/godot/releases/download/4.2.2-stable/Godot_v4.2.2-stable_win64.exe.zip"
cd /d "%ROOT%"

if not exist "%GODOT%" (
    echo Godot was not found locally. Downloading Godot 4.2.2...
    echo This happens only on the first launch.
    echo.
    if not exist "%GODOT_DIR%" mkdir "%GODOT_DIR%"
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri '%GODOT_URL%' -OutFile '%GODOT_ZIP%'; Expand-Archive -Path '%GODOT_ZIP%' -DestinationPath '%GODOT_DIR%' -Force"
    if errorlevel 1 (
        echo.
        echo Failed to download or extract Godot.
        echo Check your internet connection, then run this file again.
        pause
        exit /b 1
    )
)

if not exist "%GODOT%" (
    echo Godot executable is still missing:
    echo %GODOT%
    pause
    exit /b 1
)

echo Starting Silent Exit...
echo If the game window opens behind this console, switch to it with Alt+Tab.
echo.

"%GODOT%" --path "." --rendering-driver opengl3
set "EXIT_CODE=%ERRORLEVEL%"

echo.
echo Silent Exit closed. Exit code: %EXIT_CODE%
pause
exit /b %EXIT_CODE%

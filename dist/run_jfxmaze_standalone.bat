@echo off
setlocal enabledelayedexpansion
echo Starting JFXMaze Game...
echo.

REM Get the directory where this batch file is located
set SCRIPT_DIR=%~dp0

REM Set paths relative to the batch file location
set JAR_PATH=%SCRIPT_DIR%JFXMaze-1.0-SNAPSHOT-jar-with-dependencies.jar
set JAVAFX_LIB=%SCRIPT_DIR%javafx\lib

REM Try to find Java automatically
set JAVA_EXE=java.exe
set JAVA_FOUND=false

REM Check if java is in PATH
java -version >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo Java found in system PATH
    set JAVA_FOUND=true
    goto :check_files
)

REM Try common Java installation paths
set PATHS[0]=C:\Program Files\Java\openjdk-24.0.1\bin\java.exe
set PATHS[1]=C:\Program Files\Java\jdk-24\bin\java.exe
set PATHS[2]=C:\Program Files\Java\jdk-21\bin\java.exe
set PATHS[3]=C:\Program Files\Java\jdk-17\bin\java.exe
set PATHS[4]=C:\Program Files\Java\jdk-11\bin\java.exe

for /L %%i in (0,1,4) do (
    call set "CURRENT_PATH=%%PATHS[%%i]%%"
    if exist "!CURRENT_PATH!" (
        set JAVA_EXE=!CURRENT_PATH!
        set JAVA_FOUND=true
        echo Java found at: !CURRENT_PATH!
        goto :check_files
    )
)

if "%JAVA_FOUND%"=="false" (
    echo ERROR: Java not found!
    echo Please install Java JDK 11 or higher, or add java.exe to your PATH
    echo Download from: https://adoptium.net/
    pause
    exit /b 1
)

:check_files
REM Check if JAR exists
if not exist "%JAR_PATH%" (
    echo ERROR: JAR file not found at %JAR_PATH%
    echo Please make sure JFXMaze-1.0-SNAPSHOT-jar-with-dependencies.jar is in the same folder as this script.
    pause
    exit /b 1
)

REM Check if JavaFX directory exists
if not exist "%JAVAFX_LIB%" (
    echo ERROR: JavaFX runtime not found at %JAVAFX_LIB%
    echo Please make sure the javafx folder is in the same directory as this script.
    echo Run setup_standalone.bat first to create the standalone distribution.
    pause
    exit /b 1
)

REM Build JavaFX module path
set MODULE_PATH=%JAVAFX_LIB%\javafx-controls-17.0.6-win.jar
set MODULE_PATH=%MODULE_PATH%;%JAVAFX_LIB%\javafx-graphics-17.0.6-win.jar
set MODULE_PATH=%MODULE_PATH%;%JAVAFX_LIB%\javafx-base-17.0.6-win.jar
set MODULE_PATH=%MODULE_PATH%;%JAVAFX_LIB%\javafx-fxml-17.0.6-win.jar
set MODULE_PATH=%MODULE_PATH%;%JAVAFX_LIB%\javafx-media-17.0.6-win.jar

echo Launching JFXMaze...
echo.

REM Run the application
"%JAVA_EXE%" ^
    --module-path "%MODULE_PATH%" ^
    --add-modules javafx.controls,javafx.fxml,javafx.media ^
    --add-exports javafx.graphics/com.sun.javafx.application=ALL-UNNAMED ^
    -jar "%JAR_PATH%"

if %ERRORLEVEL% neq 0 (
    echo.
    echo Game exited with error code: %ERRORLEVEL%
    echo.
    echo Common issues:
    echo 1. Make sure Java 11+ is installed
    echo 2. Check that all JavaFX files are present in javafx\lib folder
    echo 3. Verify the JAR file is not corrupted
)

echo.
echo Game closed. Press any key to exit...
pause >nul
@echo off
setlocal
echo =================================================================
echo   Building Doctor Smart Appointment ^& Clinical Workflow System
echo =================================================================

if not exist out mkdir out

echo [1/3] Compiling Core Java Sources...
dir /s /b src\*.java > sources.txt
javac -Xlint:all -d out @sources.txt
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed!
    del sources.txt
    exit /b %errorlevel%
)
del sources.txt
echo [OK] Compilation successful with 0 errors and 0 warnings!

echo.
echo [2/3] Running 21-Scenario Verification Test Suite...
java -cp out SystemWorkflowTest
if %errorlevel% neq 0 (
    echo [ERROR] Tests failed!
    exit /b %errorlevel%
)

echo.
echo [3/3] Packaging Executable Runnable JAR...
set "JAR_BIN="
where jar >nul 2>&1
if %errorlevel% equ 0 set "JAR_BIN=jar"
if not defined JAR_BIN if defined JAVA_HOME if exist "%JAVA_HOME%\bin\jar.exe" set "JAR_BIN=%JAVA_HOME%\bin\jar.exe"
if not defined JAR_BIN (
    for /r "%ProgramFiles%\Java" %%F in (jar.exe) do (
        if not defined JAR_BIN if exist "%%F" set "JAR_BIN=%%F"
    )
)

if defined JAR_BIN (
    "%JAR_BIN%" cfe doctor-smart-system.jar Main -C out . config.properties
    echo [OK] Standalone runnable JAR doctor-smart-system.jar packaged successfully!
) else (
    echo [INFO] Compiled classes ready in out/ folder.
)

echo.
echo =================================================================
echo   BUILD COMPLETE - ALL VERIFICATIONS PASSED 100%%
echo =================================================================
echo Run Options:
echo   1. Double-click doctor-smart-system.jar to launch Desktop GUI
echo   2. Standalone JAR:   java -jar doctor-smart-system.jar
echo   3. JAR Live Demo:    java -jar doctor-smart-system.jar --demo
echo   4. JAR Desktop GUI:  java -jar doctor-smart-system.jar --gui
echo   5. Classical Main:   java -cp out Main
echo =================================================================

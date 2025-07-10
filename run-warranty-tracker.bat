@echo off
echo ========================================
echo   Digital Warranty Tracker
echo ========================================
echo.

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17+ and try again
    pause
    exit /b 1
)

echo Java detected - starting application...
echo.

REM Run the application
java -jar digital-warranty-tracker-1.0.0-jar-with-dependencies.jar

echo.
echo Application closed.
pause

#!/bin/bash

# Digital Warranty Tracker - Run Script
# This script builds and runs the warranty tracker application

echo "========================================"
echo "  Digital Warranty Tracker"
echo "========================================"
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed or not in PATH"
    echo "Please install Java 11 or higher and try again"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F '.' '{print $1}')
if [ "$JAVA_VERSION" -lt 11 ]; then
    echo "❌ Java 11 or higher is required"
    echo "Current Java version: $(java -version 2>&1 | awk -F '"' '/version/ {print $2}')"
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed or not in PATH"
    echo "Please install Maven and try again"
    exit 1
fi

echo "✅ Java $(java -version 2>&1 | awk -F '"' '/version/ {print $2}') detected"
echo "✅ Maven $(mvn -version 2>&1 | grep "Apache Maven" | awk '{print $3}') detected"
echo ""

# Check for Tesseract (optional)
if command -v tesseract &> /dev/null; then
    echo "✅ Tesseract OCR $(tesseract --version 2>&1 | head -1 | awk '{print $2}') detected"
else
    echo "⚠️  Tesseract OCR not found - OCR features will not work"
    echo "   Install with: sudo apt-get install tesseract-ocr tesseract-ocr-eng"
fi
echo ""

# Build the application
echo "🔨 Building application..."
if mvn clean compile > build.log 2>&1; then
    echo "✅ Build successful"
else
    echo "❌ Build failed. Check build.log for details"
    exit 1
fi

echo ""
echo "🚀 Starting Digital Warranty Tracker..."
echo ""

# Run the application
mvn exec:java -Dexec.mainClass="com.warrantytracker.WarrantyTrackerApp" -q

echo ""
echo "Application closed."
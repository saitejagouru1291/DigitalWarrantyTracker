# Digital Warranty Tracker

A comprehensive Java GUI application for managing warranties with OCR (Optical Character Recognition) capabilities using Tesseract and SQLite database storage.

## Features

- **Warranty Management**: Add, edit, delete, and view warranties
- **OCR Document Scanning**: Extract warranty information from receipts and documents using Tesseract OCR
- **Database Storage**: SQLite database for persistent warranty data storage
- **Search & Filter**: Search warranties by product name, brand, model, retailer, etc.
- **Expiration Tracking**: Track expired warranties and those expiring soon
- **Date Management**: Automatic warranty end date calculation
- **Multi-format Support**: Support for various image formats (JPG, PNG, BMP, TIFF)
- **Modern GUI**: Clean Java Swing interface with table sorting and filtering

## Technology Stack

- **Java 11+**: Core application language
- **Java Swing**: GUI framework
- **Tesseract OCR (Tess4j)**: Document text extraction
- **SQLite**: Database storage
- **Maven**: Dependency management and build tool
- **SLF4J**: Logging framework

## Prerequisites

Before running the application, ensure you have:

1. **Java 11 or higher** installed
2. **Maven** for building the project
3. **Tesseract OCR** installed on your system:
   - **Linux**: `sudo apt-get install tesseract-ocr tesseract-ocr-eng`
   - **macOS**: `brew install tesseract`
   - **Windows**: Download from [GitHub Tesseract releases](https://github.com/UB-Mannheim/tesseract/wiki)

## Installation & Setup

1. **Clone or download** this repository
2. **Navigate** to the project directory
3. **Build** the application:
   ```bash
   mvn clean compile
   ```

## Running the Application

### Option 1: Using Maven
```bash
mvn exec:java -Dexec.mainClass="com.warrantytracker.WarrantyTrackerApp"
```

### Option 2: Build and run JAR
```bash
# Build the JAR with dependencies
mvn clean package

# Run the application
java -jar target/digital-warranty-tracker-1.0.0-jar-with-dependencies.jar
```

### Option 3: Using the provided script
```bash
chmod +x run-app.sh
./run-app.sh
```

## Usage Guide

### Adding Warranties Manually

1. Click **"Add Warranty"** button or use the menu: Edit → Add Warranty
2. Fill in the warranty information:
   - Product Name (required)
   - Brand, Model, Serial Number
   - Purchase Date
   - Warranty Period (in months)
   - Retailer, Price, Category
   - Notes and Document Path
3. Click **"Save"** to store the warranty

### Using OCR to Extract Warranty Information

1. Click **"OCR Scan"** button or use Tools → OCR Scan
2. Select an image file (JPG, PNG, BMP, TIFF) containing warranty/receipt information
3. Wait for OCR processing to complete
4. Review and edit the extracted information in the form dialog
5. Click **"Save"** to add the warranty to your database

### Managing Warranties

- **Edit**: Select a warranty and click "Edit" or double-click the row
- **Delete**: Select a warranty and click "Delete" (with confirmation)
- **Search**: Enter search terms in the search box to filter warranties
- **View Filters**: Use the View menu to show expired or expiring warranties

### Database

The application creates a SQLite database file (`warranty_tracker.db`) in the application directory. This file contains all your warranty data and is automatically created on first run.

## Project Structure

```
src/main/java/com/warrantytracker/
├── WarrantyTrackerApp.java          # Main application entry point
├── model/
│   └── Warranty.java                # Warranty data model
├── dao/
│   └── WarrantyDAO.java            # Database access layer
├── gui/
│   ├── WarrantyTrackerMainFrame.java    # Main application window
│   ├── WarrantyFormDialog.java         # Add/Edit warranty dialog
│   └── WarrantyTableModel.java         # Table data model
└── ocr/
    └── OCRService.java             # OCR processing service
```

## Dependencies

Key dependencies managed by Maven:

- **Tess4j 5.8.0**: Tesseract OCR Java wrapper
- **SQLite JDBC 3.42.0.0**: Database connectivity
- **JCalendar 1.4**: Date picker component
- **Commons IO 2.13.0**: File operations
- **SLF4J 2.0.7**: Logging framework

## Troubleshooting

### Common Issues

1. **Tesseract not found**: Ensure Tesseract is installed and in your system PATH
2. **Database errors**: Check file permissions in the application directory
3. **GUI issues**: Ensure you're using Java 11 or higher
4. **OCR errors**: Verify image quality and supported formats

### Logs

Application logs are output to the console. For debugging, check the log messages for detailed error information.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is open source and available under the MIT License.

## Contact

For issues, questions, or contributions, please create an issue in the repository.

---

**Note**: This application requires Tesseract OCR to be installed on your system for the OCR functionality to work properly. The application will still function for manual warranty entry even without Tesseract installed.
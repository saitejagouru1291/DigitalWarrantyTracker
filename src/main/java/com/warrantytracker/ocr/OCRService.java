package com.warrantytracker.ocr;

import com.warrantytracker.model.Warranty;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OCRService {
    private static final Logger logger = LoggerFactory.getLogger(OCRService.class);
    private final Tesseract tesseract;
    
    // Common date patterns found in receipts
    private static final Pattern[] DATE_PATTERNS = {
        Pattern.compile("(\\d{1,2})[/-](\\d{1,2})[/-](\\d{4})"),  // MM/DD/YYYY or MM-DD-YYYY
        Pattern.compile("(\\d{4})[/-](\\d{1,2})[/-](\\d{1,2})"),  // YYYY/MM/DD or YYYY-MM-DD
        Pattern.compile("(\\d{1,2})\\s+(\\w+)\\s+(\\d{4})"),      // DD Month YYYY
        Pattern.compile("(\\w+)\\s+(\\d{1,2}),?\\s+(\\d{4})")    // Month DD, YYYY
    };
    
    // Price patterns
    private static final Pattern PRICE_PATTERN = Pattern.compile("\\$([0-9,]+\\.\\d{2})");
    
    // Warranty period patterns
    private static final Pattern WARRANTY_PATTERN = Pattern.compile("(?i)(\\d+)\\s*(?:year|yr|month|mo|day)s?\\s*warranty");

    public OCRService() {
        tesseract = new Tesseract();
        initializeTesseract();
    }

    private void initializeTesseract() {
        try {
            // Set the path to tessdata if needed (for Linux systems)
            // tesseract.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata");
            
            // Use English as default language
            tesseract.setLanguage("eng");
            
            // Set OCR Engine Mode (LSTM only)
            tesseract.setOcrEngineMode(1);
            
            // Set Page Segmentation Mode (automatic)
            tesseract.setPageSegMode(3);
            
            logger.info("Tesseract OCR initialized successfully");
        } catch (Exception e) {
            logger.error("Error initializing Tesseract", e);
            throw new RuntimeException("Failed to initialize OCR service", e);
        }
    }

    public String extractTextFromImage(File imageFile) throws OCRException {
        try {
            if (!imageFile.exists()) {
                throw new OCRException("Image file does not exist: " + imageFile.getPath());
            }

            // Check if file is a valid image
            BufferedImage image = ImageIO.read(imageFile);
            if (image == null) {
                throw new OCRException("Invalid image file: " + imageFile.getPath());
            }

            logger.info("Extracting text from image: {}", imageFile.getName());
            String extractedText = tesseract.doOCR(imageFile);
            
            if (extractedText == null || extractedText.trim().isEmpty()) {
                logger.warn("No text extracted from image: {}", imageFile.getName());
                return "";
            }
            
            logger.info("Successfully extracted {} characters from image", extractedText.length());
            return extractedText;
            
        } catch (TesseractException e) {
            logger.error("Tesseract OCR error for file: {}", imageFile.getPath(), e);
            throw new OCRException("OCR processing failed: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.error("IO error reading image file: {}", imageFile.getPath(), e);
            throw new OCRException("Error reading image file: " + e.getMessage(), e);
        }
    }

    public Warranty extractWarrantyInfo(File imageFile) throws OCRException {
        String extractedText = extractTextFromImage(imageFile);
        return parseWarrantyInfo(extractedText, imageFile.getPath());
    }

    public Warranty parseWarrantyInfo(String text, String documentPath) {
        if (text == null || text.trim().isEmpty()) {
            return new Warranty();
        }

        Warranty warranty = new Warranty();
        warranty.setDocumentPath(documentPath);
        
        // Clean up the text
        String cleanText = text.replaceAll("\\s+", " ").trim();
        String[] lines = cleanText.split("\\n");
        
        // Extract product information
        extractProductInfo(warranty, cleanText, lines);
        
        // Extract dates
        extractDates(warranty, cleanText);
        
        // Extract price
        extractPrice(warranty, cleanText);
        
        // Extract warranty period
        extractWarrantyPeriod(warranty, cleanText);
        
        // Set default values if not found
        setDefaults(warranty);
        
        logger.info("Parsed warranty info: Product={}, Brand={}, Date={}", 
                   warranty.getProductName(), warranty.getBrand(), warranty.getPurchaseDate());
        
        return warranty;
    }

    private void extractProductInfo(Warranty warranty, String text, String[] lines) {
        // Try to find brand and product name
        String[] commonBrands = {"Apple", "Samsung", "Sony", "Dell", "HP", "Lenovo", "ASUS", 
                                "Acer", "LG", "Canon", "Nikon", "Microsoft", "Nintendo", "Amazon"};
        
        for (String brand : commonBrands) {
            if (text.toLowerCase().contains(brand.toLowerCase())) {
                warranty.setBrand(brand);
                break;
            }
        }
        
        // Extract product name from first few lines (heuristic)
        for (int i = 0; i < Math.min(5, lines.length); i++) {
            String line = lines[i].trim();
            if (line.length() > 5 && line.length() < 100 && 
                !line.matches(".*\\d{1,2}[/-]\\d{1,2}[/-]\\d{4}.*") && // Not a date line
                !line.matches(".*\\$\\d+\\.\\d{2}.*")) { // Not a price line
                
                if (warranty.getProductName() == null || warranty.getProductName().isEmpty()) {
                    warranty.setProductName(line);
                } else if (warranty.getModel() == null || warranty.getModel().isEmpty()) {
                    warranty.setModel(line);
                }
            }
        }
        
        // If no product name found, use a generic one
        if (warranty.getProductName() == null || warranty.getProductName().isEmpty()) {
            warranty.setProductName("Product from OCR scan");
        }
    }

    private void extractDates(Warranty warranty, String text) {
        for (Pattern pattern : DATE_PATTERNS) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                LocalDate date = parseDate(matcher.group());
                if (date != null) {
                    warranty.setPurchaseDate(date);
                    break;
                }
            }
        }
    }

    private LocalDate parseDate(String dateStr) {
        DateTimeFormatter[] formatters = {
            DateTimeFormatter.ofPattern("M/d/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("M-d-yyyy"),
            DateTimeFormatter.ofPattern("MM-dd-yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("d MMMM yyyy"),
            DateTimeFormatter.ofPattern("MMMM d, yyyy")
        };
        
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                // Try next formatter
            }
        }
        
        return null;
    }

    private void extractPrice(Warranty warranty, String text) {
        Matcher matcher = PRICE_PATTERN.matcher(text);
        if (matcher.find()) {
            try {
                String priceStr = matcher.group(1).replace(",", "");
                double price = Double.parseDouble(priceStr);
                warranty.setPurchasePrice(price);
            } catch (NumberFormatException e) {
                logger.warn("Could not parse price: {}", matcher.group(1));
            }
        }
    }

    private void extractWarrantyPeriod(Warranty warranty, String text) {
        Matcher matcher = WARRANTY_PATTERN.matcher(text);
        if (matcher.find()) {
            try {
                int period = Integer.parseInt(matcher.group(1));
                String unit = matcher.group().toLowerCase();
                
                if (unit.contains("year") || unit.contains("yr")) {
                    warranty.setWarrantyPeriodMonths(period * 12);
                } else if (unit.contains("month") || unit.contains("mo")) {
                    warranty.setWarrantyPeriodMonths(period);
                } else if (unit.contains("day")) {
                    // Convert days to months (approximate)
                    warranty.setWarrantyPeriodMonths(Math.max(1, period / 30));
                }
            } catch (NumberFormatException e) {
                logger.warn("Could not parse warranty period: {}", matcher.group());
            }
        }
    }

    private void setDefaults(Warranty warranty) {
        if (warranty.getPurchaseDate() == null) {
            warranty.setPurchaseDate(LocalDate.now());
        }
        
        if (warranty.getWarrantyPeriodMonths() == 0) {
            warranty.setWarrantyPeriodMonths(12); // Default 1 year warranty
        }
        
        if (warranty.getCategory() == null || warranty.getCategory().isEmpty()) {
            warranty.setCategory("Electronics");
        }
        
        warranty.setNotes("Extracted via OCR on " + LocalDate.now());
    }

    public boolean isImageFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }
        
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || 
               name.endsWith(".png") || name.endsWith(".bmp") || 
               name.endsWith(".tiff") || name.endsWith(".tif") ||
               name.endsWith(".gif");
    }

    public static class OCRException extends Exception {
        public OCRException(String message) {
            super(message);
        }
        
        public OCRException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
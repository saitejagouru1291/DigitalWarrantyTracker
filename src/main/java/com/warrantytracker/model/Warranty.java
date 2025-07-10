package com.warrantytracker.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Warranty {
    private int id;
    private String productName;
    private String brand;
    private String model;
    private String serialNumber;
    private LocalDate purchaseDate;
    private int warrantyPeriodMonths;
    private LocalDate warrantyEndDate;
    private String retailer;
    private double purchasePrice;
    private String category;
    private String notes;
    private String documentPath;
    private LocalDate createdDate;

    // Default constructor
    public Warranty() {
        this.createdDate = LocalDate.now();
    }

    // Constructor with essential fields
    public Warranty(String productName, String brand, LocalDate purchaseDate, int warrantyPeriodMonths) {
        this();
        this.productName = productName;
        this.brand = brand;
        this.purchaseDate = purchaseDate;
        this.warrantyPeriodMonths = warrantyPeriodMonths;
        calculateWarrantyEndDate();
    }

    // Full constructor
    public Warranty(int id, String productName, String brand, String model, String serialNumber,
                   LocalDate purchaseDate, int warrantyPeriodMonths, String retailer,
                   double purchasePrice, String category, String notes, String documentPath) {
        this();
        this.id = id;
        this.productName = productName;
        this.brand = brand;
        this.model = model;
        this.serialNumber = serialNumber;
        this.purchaseDate = purchaseDate;
        this.warrantyPeriodMonths = warrantyPeriodMonths;
        this.retailer = retailer;
        this.purchasePrice = purchasePrice;
        this.category = category;
        this.notes = notes;
        this.documentPath = documentPath;
        calculateWarrantyEndDate();
    }

    private void calculateWarrantyEndDate() {
        if (purchaseDate != null && warrantyPeriodMonths > 0) {
            this.warrantyEndDate = purchaseDate.plusMonths(warrantyPeriodMonths);
        }
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
        calculateWarrantyEndDate();
    }

    public int getWarrantyPeriodMonths() {
        return warrantyPeriodMonths;
    }

    public void setWarrantyPeriodMonths(int warrantyPeriodMonths) {
        this.warrantyPeriodMonths = warrantyPeriodMonths;
        calculateWarrantyEndDate();
    }

    public LocalDate getWarrantyEndDate() {
        return warrantyEndDate;
    }

    public void setWarrantyEndDate(LocalDate warrantyEndDate) {
        this.warrantyEndDate = warrantyEndDate;
    }

    public String getRetailer() {
        return retailer;
    }

    public void setRetailer(String retailer) {
        this.retailer = retailer;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDocumentPath() {
        return documentPath;
    }

    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    // Utility methods
    public boolean isExpired() {
        return warrantyEndDate != null && LocalDate.now().isAfter(warrantyEndDate);
    }

    public boolean isExpiringSoon(int days) {
        if (warrantyEndDate == null) return false;
        LocalDate threshold = LocalDate.now().plusDays(days);
        return warrantyEndDate.isBefore(threshold) && !isExpired();
    }

    public String getFormattedPurchaseDate() {
        return purchaseDate != null ? purchaseDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "";
    }

    public String getFormattedWarrantyEndDate() {
        return warrantyEndDate != null ? warrantyEndDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "";
    }

    @Override
    public String toString() {
        return String.format("%s - %s (%s)", productName, brand, 
                           isExpired() ? "EXPIRED" : getFormattedWarrantyEndDate());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Warranty warranty = (Warranty) obj;
        return id == warranty.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
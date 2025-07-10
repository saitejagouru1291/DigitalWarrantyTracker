package com.warrantytracker.gui;

import com.warrantytracker.model.Warranty;

import javax.swing.table.AbstractTableModel;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WarrantyTableModel extends AbstractTableModel {
    private static final String[] COLUMN_NAMES = {
        "ID", "Product Name", "Brand", "Model", "Purchase Date", 
        "Warranty End", "Status", "Price", "Category", "Retailer"
    };
    
    private static final Class<?>[] COLUMN_CLASSES = {
        Integer.class, String.class, String.class, String.class, String.class,
        String.class, String.class, String.class, String.class, String.class
    };
    
    private List<Warranty> warranties;
    
    public WarrantyTableModel() {
        this.warranties = new ArrayList<>();
    }
    
    public WarrantyTableModel(List<Warranty> warranties) {
        this.warranties = warranties != null ? new ArrayList<>(warranties) : new ArrayList<>();
    }
    
    @Override
    public int getRowCount() {
        return warranties.size();
    }
    
    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }
    
    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return COLUMN_CLASSES[columnIndex];
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= warranties.size()) {
            return null;
        }
        
        Warranty warranty = warranties.get(rowIndex);
        
        switch (columnIndex) {
            case 0: return warranty.getId();
            case 1: return warranty.getProductName();
            case 2: return warranty.getBrand();
            case 3: return warranty.getModel();
            case 4: return warranty.getFormattedPurchaseDate();
            case 5: return warranty.getFormattedWarrantyEndDate();
            case 6: return getWarrantyStatus(warranty);
            case 7: return formatPrice(warranty.getPurchasePrice());
            case 8: return warranty.getCategory();
            case 9: return warranty.getRetailer();
            default: return null;
        }
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false; // Make table read-only
    }
    
    private String getWarrantyStatus(Warranty warranty) {
        if (warranty.isExpired()) {
            return "EXPIRED";
        } else if (warranty.isExpiringSoon(30)) {
            return "EXPIRING SOON";
        } else {
            return "ACTIVE";
        }
    }
    
    private String formatPrice(double price) {
        if (price > 0) {
            return String.format("$%.2f", price);
        }
        return "";
    }
    
    public void setWarranties(List<Warranty> warranties) {
        this.warranties = warranties != null ? new ArrayList<>(warranties) : new ArrayList<>();
        fireTableDataChanged();
    }
    
    public void addWarranty(Warranty warranty) {
        if (warranty != null) {
            warranties.add(warranty);
            int row = warranties.size() - 1;
            fireTableRowsInserted(row, row);
        }
    }
    
    public void updateWarranty(int index, Warranty warranty) {
        if (index >= 0 && index < warranties.size() && warranty != null) {
            warranties.set(index, warranty);
            fireTableRowsUpdated(index, index);
        }
    }
    
    public void removeWarranty(int index) {
        if (index >= 0 && index < warranties.size()) {
            warranties.remove(index);
            fireTableRowsDeleted(index, index);
        }
    }
    
    public Warranty getWarrantyAt(int index) {
        if (index >= 0 && index < warranties.size()) {
            return warranties.get(index);
        }
        return null;
    }
    
    public List<Warranty> getAllWarranties() {
        return new ArrayList<>(warranties);
    }
    
    public void clear() {
        int size = warranties.size();
        if (size > 0) {
            warranties.clear();
            fireTableRowsDeleted(0, size - 1);
        }
    }
    
    public int findWarrantyIndex(int warrantyId) {
        for (int i = 0; i < warranties.size(); i++) {
            if (warranties.get(i).getId() == warrantyId) {
                return i;
            }
        }
        return -1;
    }
}
package com.warrantytracker.dao;

import com.warrantytracker.model.Warranty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WarrantyDAO {
    private static final Logger logger = LoggerFactory.getLogger(WarrantyDAO.class);
    private static final String DB_URL = "jdbc:sqlite:warranty_tracker.db";

    public WarrantyDAO() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS warranties (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                product_name TEXT NOT NULL,
                brand TEXT,
                model TEXT,
                serial_number TEXT,
                purchase_date DATE,
                warranty_period_months INTEGER,
                warranty_end_date DATE,
                retailer TEXT,
                purchase_price REAL,
                category TEXT,
                notes TEXT,
                document_path TEXT,
                created_date DATE DEFAULT CURRENT_DATE
            )
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            logger.info("Database initialized successfully");
        } catch (SQLException e) {
            logger.error("Error initializing database", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public int addWarranty(Warranty warranty) {
        String sql = """
            INSERT INTO warranties (product_name, brand, model, serial_number, purchase_date,
                                  warranty_period_months, warranty_end_date, retailer,
                                  purchase_price, category, notes, document_path, created_date)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, warranty.getProductName());
            pstmt.setString(2, warranty.getBrand());
            pstmt.setString(3, warranty.getModel());
            pstmt.setString(4, warranty.getSerialNumber());
            pstmt.setDate(5, warranty.getPurchaseDate() != null ? Date.valueOf(warranty.getPurchaseDate()) : null);
            pstmt.setInt(6, warranty.getWarrantyPeriodMonths());
            pstmt.setDate(7, warranty.getWarrantyEndDate() != null ? Date.valueOf(warranty.getWarrantyEndDate()) : null);
            pstmt.setString(8, warranty.getRetailer());
            pstmt.setDouble(9, warranty.getPurchasePrice());
            pstmt.setString(10, warranty.getCategory());
            pstmt.setString(11, warranty.getNotes());
            pstmt.setString(12, warranty.getDocumentPath());
            pstmt.setDate(13, warranty.getCreatedDate() != null ? Date.valueOf(warranty.getCreatedDate()) : Date.valueOf(LocalDate.now()));

            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        warranty.setId(id);
                        logger.info("Warranty added successfully with ID: {}", id);
                        return id;
                    }
                }
            }
            
            throw new SQLException("Creating warranty failed, no ID obtained.");
            
        } catch (SQLException e) {
            logger.error("Error adding warranty", e);
            throw new RuntimeException("Failed to add warranty", e);
        }
    }

    public Warranty getWarrantyById(int id) {
        String sql = "SELECT * FROM warranties WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToWarranty(rs);
            }
            
            return null;
            
        } catch (SQLException e) {
            logger.error("Error getting warranty by ID: {}", id, e);
            throw new RuntimeException("Failed to get warranty", e);
        }
    }

    public List<Warranty> getAllWarranties() {
        String sql = "SELECT * FROM warranties ORDER BY warranty_end_date ASC";
        List<Warranty> warranties = new ArrayList<>();
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                warranties.add(mapResultSetToWarranty(rs));
            }
            
            logger.info("Retrieved {} warranties", warranties.size());
            return warranties;
            
        } catch (SQLException e) {
            logger.error("Error getting all warranties", e);
            throw new RuntimeException("Failed to get warranties", e);
        }
    }

    public boolean updateWarranty(Warranty warranty) {
        String sql = """
            UPDATE warranties SET product_name = ?, brand = ?, model = ?, serial_number = ?,
                                purchase_date = ?, warranty_period_months = ?, warranty_end_date = ?,
                                retailer = ?, purchase_price = ?, category = ?, notes = ?, document_path = ?
            WHERE id = ?
        """;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, warranty.getProductName());
            pstmt.setString(2, warranty.getBrand());
            pstmt.setString(3, warranty.getModel());
            pstmt.setString(4, warranty.getSerialNumber());
            pstmt.setDate(5, warranty.getPurchaseDate() != null ? Date.valueOf(warranty.getPurchaseDate()) : null);
            pstmt.setInt(6, warranty.getWarrantyPeriodMonths());
            pstmt.setDate(7, warranty.getWarrantyEndDate() != null ? Date.valueOf(warranty.getWarrantyEndDate()) : null);
            pstmt.setString(8, warranty.getRetailer());
            pstmt.setDouble(9, warranty.getPurchasePrice());
            pstmt.setString(10, warranty.getCategory());
            pstmt.setString(11, warranty.getNotes());
            pstmt.setString(12, warranty.getDocumentPath());
            pstmt.setInt(13, warranty.getId());

            int affectedRows = pstmt.executeUpdate();
            boolean success = affectedRows > 0;
            
            if (success) {
                logger.info("Warranty updated successfully: {}", warranty.getId());
            }
            
            return success;
            
        } catch (SQLException e) {
            logger.error("Error updating warranty: {}", warranty.getId(), e);
            throw new RuntimeException("Failed to update warranty", e);
        }
    }

    public boolean deleteWarranty(int id) {
        String sql = "DELETE FROM warranties WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            boolean success = affectedRows > 0;
            
            if (success) {
                logger.info("Warranty deleted successfully: {}", id);
            }
            
            return success;
            
        } catch (SQLException e) {
            logger.error("Error deleting warranty: {}", id, e);
            throw new RuntimeException("Failed to delete warranty", e);
        }
    }

    public List<Warranty> searchWarranties(String searchTerm) {
        String sql = """
            SELECT * FROM warranties 
            WHERE product_name LIKE ? OR brand LIKE ? OR model LIKE ? 
                  OR serial_number LIKE ? OR retailer LIKE ? OR category LIKE ?
            ORDER BY warranty_end_date ASC
        """;
        
        List<Warranty> warranties = new ArrayList<>();
        String searchPattern = "%" + searchTerm + "%";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            for (int i = 1; i <= 6; i++) {
                pstmt.setString(i, searchPattern);
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                warranties.add(mapResultSetToWarranty(rs));
            }
            
            logger.info("Search for '{}' returned {} results", searchTerm, warranties.size());
            return warranties;
            
        } catch (SQLException e) {
            logger.error("Error searching warranties", e);
            throw new RuntimeException("Failed to search warranties", e);
        }
    }

    public List<Warranty> getExpiredWarranties() {
        String sql = "SELECT * FROM warranties WHERE warranty_end_date < ? ORDER BY warranty_end_date DESC";
        List<Warranty> warranties = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, Date.valueOf(LocalDate.now()));
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                warranties.add(mapResultSetToWarranty(rs));
            }
            
            return warranties;
            
        } catch (SQLException e) {
            logger.error("Error getting expired warranties", e);
            throw new RuntimeException("Failed to get expired warranties", e);
        }
    }

    public List<Warranty> getExpiringSoonWarranties(int days) {
        String sql = """
            SELECT * FROM warranties 
            WHERE warranty_end_date BETWEEN ? AND ?
            ORDER BY warranty_end_date ASC
        """;
        
        List<Warranty> warranties = new ArrayList<>();
        LocalDate now = LocalDate.now();
        LocalDate threshold = now.plusDays(days);
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, Date.valueOf(now));
            pstmt.setDate(2, Date.valueOf(threshold));
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                warranties.add(mapResultSetToWarranty(rs));
            }
            
            return warranties;
            
        } catch (SQLException e) {
            logger.error("Error getting expiring warranties", e);
            throw new RuntimeException("Failed to get expiring warranties", e);
        }
    }

    private Warranty mapResultSetToWarranty(ResultSet rs) throws SQLException {
        Warranty warranty = new Warranty();
        warranty.setId(rs.getInt("id"));
        warranty.setProductName(rs.getString("product_name"));
        warranty.setBrand(rs.getString("brand"));
        warranty.setModel(rs.getString("model"));
        warranty.setSerialNumber(rs.getString("serial_number"));
        
        Date purchaseDate = rs.getDate("purchase_date");
        if (purchaseDate != null) {
            warranty.setPurchaseDate(purchaseDate.toLocalDate());
        }
        
        warranty.setWarrantyPeriodMonths(rs.getInt("warranty_period_months"));
        
        Date warrantyEndDate = rs.getDate("warranty_end_date");
        if (warrantyEndDate != null) {
            warranty.setWarrantyEndDate(warrantyEndDate.toLocalDate());
        }
        
        warranty.setRetailer(rs.getString("retailer"));
        warranty.setPurchasePrice(rs.getDouble("purchase_price"));
        warranty.setCategory(rs.getString("category"));
        warranty.setNotes(rs.getString("notes"));
        warranty.setDocumentPath(rs.getString("document_path"));
        
        Date createdDate = rs.getDate("created_date");
        if (createdDate != null) {
            warranty.setCreatedDate(createdDate.toLocalDate());
        }
        
        return warranty;
    }

    public void closeDatabase() {
        // SQLite automatically closes connections when not in use
        logger.info("Database connection closed");
    }
}
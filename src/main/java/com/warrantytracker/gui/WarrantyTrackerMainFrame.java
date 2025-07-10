package com.warrantytracker.gui;

import com.warrantytracker.dao.WarrantyDAO;
import com.warrantytracker.model.Warranty;
import com.warrantytracker.ocr.OCRService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

public class WarrantyTrackerMainFrame extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(WarrantyTrackerMainFrame.class);

    private WarrantyDAO warrantyDAO;
    private OCRService ocrService;
    private WarrantyTableModel tableModel;
    private JTable warrantyTable;
    private TableRowSorter<WarrantyTableModel> sorter;
    
    // GUI Components
    private JTextField searchField;
    private JButton searchButton;
    private JButton clearSearchButton;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton ocrScanButton;
    private JButton refreshButton;
    
    // Menu components
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenu editMenu;
    private JMenu viewMenu;
    private JMenu toolsMenu;
    private JMenu helpMenu;
    
    // Status components
    private JLabel statusLabel;
    private JLabel totalWarrantiesLabel;
    private JLabel expiredLabel;
    private JLabel expiringSoonLabel;

    public WarrantyTrackerMainFrame() {
        super("Digital Warranty Tracker");
        initializeServices();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupMenuBar();
        loadWarranties();
        updateStatusLabels();
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
    }

    private void initializeServices() {
        try {
            warrantyDAO = new WarrantyDAO();
            ocrService = new OCRService();
            logger.info("Services initialized successfully");
        } catch (Exception e) {
            logger.error("Error initializing services", e);
            JOptionPane.showMessageDialog(this, 
                "Error initializing application services: " + e.getMessage(),
                "Initialization Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initializeComponents() {
        // Table setup
        tableModel = new WarrantyTableModel();
        warrantyTable = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        warrantyTable.setRowSorter(sorter);
        
        // Configure table
        warrantyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        warrantyTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        warrantyTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        warrantyTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        warrantyTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Product Name
        warrantyTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Brand
        warrantyTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Model
        warrantyTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Purchase Date
        warrantyTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Warranty End
        warrantyTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // Status
        warrantyTable.getColumnModel().getColumn(7).setPreferredWidth(80);  // Price
        warrantyTable.getColumnModel().getColumn(8).setPreferredWidth(100); // Category
        warrantyTable.getColumnModel().getColumn(9).setPreferredWidth(120); // Retailer
        
        // Search components
        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        clearSearchButton = new JButton("Clear");
        
        // Action buttons
        addButton = new JButton("Add Warranty");
        editButton = new JButton("Edit");
        deleteButton = new JButton("Delete");
        ocrScanButton = new JButton("OCR Scan");
        refreshButton = new JButton("Refresh");
        
        // Status labels
        statusLabel = new JLabel("Ready");
        totalWarrantiesLabel = new JLabel("Total: 0");
        expiredLabel = new JLabel("Expired: 0");
        expiringSoonLabel = new JLabel("Expiring Soon: 0");
        
        // Set button icons and tooltips
        addButton.setToolTipText("Add a new warranty");
        editButton.setToolTipText("Edit selected warranty");
        deleteButton.setToolTipText("Delete selected warranty");
        ocrScanButton.setToolTipText("Scan receipt/document with OCR");
        refreshButton.setToolTipText("Refresh warranty list");
        searchButton.setToolTipText("Search warranties");
        clearSearchButton.setToolTipText("Clear search results");
        
        // Initially disable edit and delete buttons
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Top panel with search
        JPanel topPanel = new JPanel(new BorderLayout());
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(new TitledBorder("Search"));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearSearchButton);
        
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.add(addButton);
        actionPanel.add(editButton);
        actionPanel.add(deleteButton);
        actionPanel.add(ocrScanButton);
        actionPanel.add(refreshButton);
        
        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(actionPanel, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);

        // Center panel with table
        JScrollPane scrollPane = new JScrollPane(warrantyTable);
        scrollPane.setBorder(new TitledBorder("Warranties"));
        add(scrollPane, BorderLayout.CENTER);

        // Bottom panel with status
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        
        JPanel leftStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftStatusPanel.add(statusLabel);
        
        JPanel rightStatusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightStatusPanel.add(totalWarrantiesLabel);
        rightStatusPanel.add(new JLabel(" | "));
        rightStatusPanel.add(expiredLabel);
        rightStatusPanel.add(new JLabel(" | "));
        rightStatusPanel.add(expiringSoonLabel);
        
        statusPanel.add(leftStatusPanel, BorderLayout.WEST);
        statusPanel.add(rightStatusPanel, BorderLayout.EAST);
        
        add(statusPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        // Table selection handler
        warrantyTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = warrantyTable.getSelectedRow() != -1;
                editButton.setEnabled(hasSelection);
                deleteButton.setEnabled(hasSelection);
            }
        });

        // Double-click to edit
        warrantyTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedWarranty();
                }
            }
        });

        // Button handlers
        addButton.addActionListener(e -> addNewWarranty());
        editButton.addActionListener(e -> editSelectedWarranty());
        deleteButton.addActionListener(e -> deleteSelectedWarranty());
        ocrScanButton.addActionListener(e -> performOCRScan());
        refreshButton.addActionListener(e -> refreshWarranties());
        searchButton.addActionListener(e -> performSearch());
        clearSearchButton.addActionListener(e -> clearSearch());
        
        // Search field enter key
        searchField.addActionListener(e -> performSearch());
    }

    private void setupMenuBar() {
        menuBar = new JMenuBar();

        // File Menu
        fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        // Edit Menu
        editMenu = new JMenu("Edit");
        JMenuItem addItem = new JMenuItem("Add Warranty");
        addItem.addActionListener(e -> addNewWarranty());
        JMenuItem editItem = new JMenuItem("Edit Warranty");
        editItem.addActionListener(e -> editSelectedWarranty());
        JMenuItem deleteItem = new JMenuItem("Delete Warranty");
        deleteItem.addActionListener(e -> deleteSelectedWarranty());
        
        editMenu.add(addItem);
        editMenu.add(editItem);
        editMenu.addSeparator();
        editMenu.add(deleteItem);

        // View Menu
        viewMenu = new JMenu("View");
        JMenuItem refreshItem = new JMenuItem("Refresh");
        refreshItem.addActionListener(e -> refreshWarranties());
        JMenuItem expiredItem = new JMenuItem("Show Expired");
        expiredItem.addActionListener(e -> showExpiredWarranties());
        JMenuItem expiringSoonItem = new JMenuItem("Show Expiring Soon");
        expiringSoonItem.addActionListener(e -> showExpiringSoonWarranties());
        
        viewMenu.add(refreshItem);
        viewMenu.addSeparator();
        viewMenu.add(expiredItem);
        viewMenu.add(expiringSoonItem);

        // Tools Menu
        toolsMenu = new JMenu("Tools");
        JMenuItem ocrItem = new JMenuItem("OCR Scan");
        ocrItem.addActionListener(e -> performOCRScan());
        toolsMenu.add(ocrItem);

        // Help Menu
        helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(toolsMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void addNewWarranty() {
        WarrantyFormDialog dialog = new WarrantyFormDialog(this, "Add New Warranty");
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            try {
                Warranty warranty = dialog.getWarranty();
                warrantyDAO.addWarranty(warranty);
                tableModel.addWarranty(warranty);
                updateStatusLabels();
                statusLabel.setText("Warranty added successfully");
                logger.info("New warranty added: {}", warranty.getProductName());
            } catch (Exception e) {
                logger.error("Error adding warranty", e);
                JOptionPane.showMessageDialog(this, 
                    "Error adding warranty: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editSelectedWarranty() {
        int selectedRow = warrantyTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        int modelRow = warrantyTable.convertRowIndexToModel(selectedRow);
        Warranty warranty = tableModel.getWarrantyAt(modelRow);
        
        if (warranty != null) {
            WarrantyFormDialog dialog = new WarrantyFormDialog(this, "Edit Warranty", warranty);
            dialog.setVisible(true);
            
            if (dialog.isConfirmed()) {
                try {
                    Warranty updatedWarranty = dialog.getWarranty();
                    warrantyDAO.updateWarranty(updatedWarranty);
                    tableModel.updateWarranty(modelRow, updatedWarranty);
                    updateStatusLabels();
                    statusLabel.setText("Warranty updated successfully");
                    logger.info("Warranty updated: {}", updatedWarranty.getProductName());
                } catch (Exception e) {
                    logger.error("Error updating warranty", e);
                    JOptionPane.showMessageDialog(this, 
                        "Error updating warranty: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void deleteSelectedWarranty() {
        int selectedRow = warrantyTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        int modelRow = warrantyTable.convertRowIndexToModel(selectedRow);
        Warranty warranty = tableModel.getWarrantyAt(modelRow);
        
        if (warranty != null) {
            int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the warranty for \"" + warranty.getProductName() + "\"?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                try {
                    warrantyDAO.deleteWarranty(warranty.getId());
                    tableModel.removeWarranty(modelRow);
                    updateStatusLabels();
                    statusLabel.setText("Warranty deleted successfully");
                    logger.info("Warranty deleted: {}", warranty.getProductName());
                } catch (Exception e) {
                    logger.error("Error deleting warranty", e);
                    JOptionPane.showMessageDialog(this, 
                        "Error deleting warranty: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void performOCRScan() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) return true;
                String name = file.getName().toLowerCase();
                return name.endsWith(".jpg") || name.endsWith(".jpeg") || 
                       name.endsWith(".png") || name.endsWith(".bmp") || 
                       name.endsWith(".tiff") || name.endsWith(".tif");
            }

            @Override
            public String getDescription() {
                return "Image files (*.jpg, *.png, *.bmp, *.tiff)";
            }
        });

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // Show progress dialog
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            JDialog progressDialog = new JDialog(this, "Processing OCR...", true);
            progressDialog.add(new JLabel("Extracting text from image..."), BorderLayout.NORTH);
            progressDialog.add(progressBar, BorderLayout.CENTER);
            progressDialog.setSize(300, 100);
            progressDialog.setLocationRelativeTo(this);
            
            SwingWorker<Warranty, Void> worker = new SwingWorker<Warranty, Void>() {
                @Override
                protected Warranty doInBackground() throws Exception {
                    return ocrService.extractWarrantyInfo(selectedFile);
                }
                
                @Override
                protected void done() {
                    progressDialog.dispose();
                    try {
                        Warranty warranty = get();
                        WarrantyFormDialog dialog = new WarrantyFormDialog(
                            WarrantyTrackerMainFrame.this, "Review OCR Results", warranty);
                        dialog.setVisible(true);
                        
                        if (dialog.isConfirmed()) {
                            Warranty finalWarranty = dialog.getWarranty();
                            warrantyDAO.addWarranty(finalWarranty);
                            tableModel.addWarranty(finalWarranty);
                            updateStatusLabels();
                            statusLabel.setText("OCR scan completed and warranty added");
                        }
                    } catch (Exception e) {
                        logger.error("Error during OCR processing", e);
                        JOptionPane.showMessageDialog(WarrantyTrackerMainFrame.this,
                            "Error processing OCR: " + e.getMessage(),
                            "OCR Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            
            worker.execute();
            progressDialog.setVisible(true);
        }
    }

    private void performSearch() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            clearSearch();
            return;
        }
        
        try {
            List<Warranty> searchResults = warrantyDAO.searchWarranties(searchTerm);
            tableModel.setWarranties(searchResults);
            updateStatusLabels();
            statusLabel.setText("Search completed: " + searchResults.size() + " results found");
        } catch (Exception e) {
            logger.error("Error during search", e);
            JOptionPane.showMessageDialog(this,
                "Error during search: " + e.getMessage(),
                "Search Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearSearch() {
        searchField.setText("");
        refreshWarranties();
    }

    private void refreshWarranties() {
        loadWarranties();
        statusLabel.setText("Warranty list refreshed");
    }

    private void showExpiredWarranties() {
        try {
            List<Warranty> expiredWarranties = warrantyDAO.getExpiredWarranties();
            tableModel.setWarranties(expiredWarranties);
            updateStatusLabels();
            statusLabel.setText("Showing expired warranties: " + expiredWarranties.size() + " found");
        } catch (Exception e) {
            logger.error("Error loading expired warranties", e);
            JOptionPane.showMessageDialog(this,
                "Error loading expired warranties: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showExpiringSoonWarranties() {
        try {
            List<Warranty> expiringSoon = warrantyDAO.getExpiringSoonWarranties(30);
            tableModel.setWarranties(expiringSoon);
            updateStatusLabels();
            statusLabel.setText("Showing warranties expiring in 30 days: " + expiringSoon.size() + " found");
        } catch (Exception e) {
            logger.error("Error loading expiring warranties", e);
            JOptionPane.showMessageDialog(this,
                "Error loading expiring warranties: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadWarranties() {
        try {
            List<Warranty> warranties = warrantyDAO.getAllWarranties();
            tableModel.setWarranties(warranties);
            updateStatusLabels();
            logger.info("Loaded {} warranties", warranties.size());
        } catch (Exception e) {
            logger.error("Error loading warranties", e);
            JOptionPane.showMessageDialog(this,
                "Error loading warranties: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStatusLabels() {
        List<Warranty> allWarranties = tableModel.getAllWarranties();
        int total = allWarranties.size();
        int expired = (int) allWarranties.stream().filter(Warranty::isExpired).count();
        int expiringSoon = (int) allWarranties.stream().filter(w -> w.isExpiringSoon(30)).count();
        
        totalWarrantiesLabel.setText("Total: " + total);
        expiredLabel.setText("Expired: " + expired);
        expiringSoonLabel.setText("Expiring Soon: " + expiringSoon);
    }

    private void showAboutDialog() {
        String aboutText = """
            Digital Warranty Tracker v1.0
            
            A comprehensive warranty management system with OCR capabilities.
            
            Features:
            • Add, edit, and delete warranties
            • OCR scanning of receipts and documents
            • Search and filter warranties
            • Track expiration dates
            • SQLite database storage
            
            Built with Java Swing, Tesseract OCR, and SQLite.
            """;
        
        JOptionPane.showMessageDialog(this, aboutText, "About", JOptionPane.INFORMATION_MESSAGE);
    }
}
package com.warrantytracker.gui;

import com.warrantytracker.model.Warranty;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class WarrantyFormDialog extends JDialog {
    private static final String[] CATEGORIES = {
        "Electronics", "Appliances", "Automotive", "Home & Garden", 
        "Tools", "Furniture", "Sports & Recreation", "Other"
    };

    private JTextField productNameField;
    private JTextField brandField;
    private JTextField modelField;
    private JTextField serialNumberField;
    private JDateChooser purchaseDateChooser;
    private JSpinner warrantyPeriodSpinner;
    private JTextField retailerField;
    private JTextField priceField;
    private JComboBox<String> categoryComboBox;
    private JTextArea notesArea;
    private JTextField documentPathField;
    private JButton browseButton;

    private JButton saveButton;
    private JButton cancelButton;

    private Warranty warranty;
    private boolean confirmed = false;

    public WarrantyFormDialog(Window parent, String title) {
        super(parent, title, ModalityType.APPLICATION_MODAL);
        this.warranty = new Warranty();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(parent);
    }

    public WarrantyFormDialog(Window parent, String title, Warranty warranty) {
        this(parent, title);
        this.warranty = warranty != null ? warranty : new Warranty();
        populateFields();
    }

    private void initializeComponents() {
        productNameField = new JTextField(20);
        brandField = new JTextField(15);
        modelField = new JTextField(15);
        serialNumberField = new JTextField(15);
        
        purchaseDateChooser = new JDateChooser();
        purchaseDateChooser.setDateFormatString("yyyy-MM-dd");
        purchaseDateChooser.setPreferredSize(new Dimension(120, 25));
        
        warrantyPeriodSpinner = new JSpinner(new SpinnerNumberModel(12, 1, 120, 1));
        
        retailerField = new JTextField(15);
        priceField = new JTextField(10);
        
        categoryComboBox = new JComboBox<>(CATEGORIES);
        categoryComboBox.setEditable(true);
        
        notesArea = new JTextArea(4, 30);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        
        documentPathField = new JTextField(25);
        documentPathField.setEditable(false);
        browseButton = new JButton("Browse...");
        
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");
        
        // Set tooltips
        productNameField.setToolTipText("Enter the product name");
        brandField.setToolTipText("Enter the brand/manufacturer");
        modelField.setToolTipText("Enter the model number");
        serialNumberField.setToolTipText("Enter the serial number");
        purchaseDateChooser.setToolTipText("Select the purchase date");
        warrantyPeriodSpinner.setToolTipText("Enter warranty period in months");
        priceField.setToolTipText("Enter purchase price (e.g., 299.99)");
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Product Information Section
        JPanel productPanel = createSection("Product Information");
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(productPanel, gbc);

        // Product Name
        gbc.gridwidth = 1; gbc.gridy++;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Product Name:*"), gbc);
        gbc.gridx = 1;
        mainPanel.add(productNameField, gbc);

        // Brand and Model
        gbc.gridy++;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Brand:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(brandField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Model:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(modelField, gbc);

        // Serial Number
        gbc.gridy++;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Serial Number:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(serialNumberField, gbc);

        // Purchase Information Section
        JPanel purchasePanel = createSection("Purchase Information");
        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2;
        mainPanel.add(purchasePanel, gbc);

        // Purchase Date
        gbc.gridwidth = 1; gbc.gridy++;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Purchase Date:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(purchaseDateChooser, gbc);

        // Warranty Period
        gbc.gridy++;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Warranty (months):"), gbc);
        gbc.gridx = 1;
        JPanel warrantyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        warrantyPanel.add(warrantyPeriodSpinner);
        warrantyPanel.add(new JLabel(" months"));
        mainPanel.add(warrantyPanel, gbc);

        // Retailer
        gbc.gridy++;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Retailer:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(retailerField, gbc);

        // Price
        gbc.gridy++;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Price:"), gbc);
        gbc.gridx = 1;
        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pricePanel.add(new JLabel("$"));
        pricePanel.add(priceField);
        mainPanel.add(pricePanel, gbc);

        // Category
        gbc.gridy++;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(categoryComboBox, gbc);

        // Document Path
        gbc.gridy++;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("Document:"), gbc);
        gbc.gridx = 1;
        JPanel documentPanel = new JPanel(new BorderLayout(5, 0));
        documentPanel.add(documentPathField, BorderLayout.CENTER);
        documentPanel.add(browseButton, BorderLayout.EAST);
        mainPanel.add(documentPanel, gbc);

        // Notes
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        mainPanel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        mainPanel.add(new JScrollPane(notesArea), gbc);

        add(mainPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createSection(String title) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), title, 
            0, 0, null, Color.BLUE));
        return panel;
    }

    private void setupEventHandlers() {
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateAndSave()) {
                    confirmed = true;
                    dispose();
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmed = false;
                dispose();
            }
        });

        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browseForDocument();
            }
        });
    }

    private void populateFields() {
        if (warranty == null) return;

        productNameField.setText(warranty.getProductName());
        brandField.setText(warranty.getBrand());
        modelField.setText(warranty.getModel());
        serialNumberField.setText(warranty.getSerialNumber());

        if (warranty.getPurchaseDate() != null) {
            Date date = Date.from(warranty.getPurchaseDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            purchaseDateChooser.setDate(date);
        }

        warrantyPeriodSpinner.setValue(warranty.getWarrantyPeriodMonths());
        retailerField.setText(warranty.getRetailer());

        if (warranty.getPurchasePrice() > 0) {
            priceField.setText(String.valueOf(warranty.getPurchasePrice()));
        }

        if (warranty.getCategory() != null) {
            categoryComboBox.setSelectedItem(warranty.getCategory());
        }

        notesArea.setText(warranty.getNotes());
        documentPathField.setText(warranty.getDocumentPath());
    }

    private boolean validateAndSave() {
        // Validate required fields
        if (productNameField.getText().trim().isEmpty()) {
            showError("Product name is required.");
            productNameField.requestFocus();
            return false;
        }

        // Validate price
        double price = 0;
        String priceText = priceField.getText().trim();
        if (!priceText.isEmpty()) {
            try {
                price = Double.parseDouble(priceText);
                if (price < 0) {
                    showError("Price cannot be negative.");
                    priceField.requestFocus();
                    return false;
                }
            } catch (NumberFormatException e) {
                showError("Invalid price format.");
                priceField.requestFocus();
                return false;
            }
        }

        // Save to warranty object
        warranty.setProductName(productNameField.getText().trim());
        warranty.setBrand(brandField.getText().trim());
        warranty.setModel(modelField.getText().trim());
        warranty.setSerialNumber(serialNumberField.getText().trim());

        Date selectedDate = purchaseDateChooser.getDate();
        if (selectedDate != null) {
            LocalDate localDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            warranty.setPurchaseDate(localDate);
        }

        warranty.setWarrantyPeriodMonths((Integer) warrantyPeriodSpinner.getValue());
        warranty.setRetailer(retailerField.getText().trim());
        warranty.setPurchasePrice(price);
        warranty.setCategory(categoryComboBox.getSelectedItem().toString());
        warranty.setNotes(notesArea.getText().trim());
        warranty.setDocumentPath(documentPathField.getText().trim());

        return true;
    }

    private void browseForDocument() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(java.io.File file) {
                if (file.isDirectory()) return true;
                String name = file.getName().toLowerCase();
                return name.endsWith(".jpg") || name.endsWith(".jpeg") || 
                       name.endsWith(".png") || name.endsWith(".pdf") || 
                       name.endsWith(".bmp") || name.endsWith(".tiff");
            }

            @Override
            public String getDescription() {
                return "Image and PDF files (*.jpg, *.png, *.pdf, etc.)";
            }
        });

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            documentPathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Warranty getWarranty() {
        return warranty;
    }
}
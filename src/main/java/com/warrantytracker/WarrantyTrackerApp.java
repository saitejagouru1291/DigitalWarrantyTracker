package com.warrantytracker;

import com.warrantytracker.gui.WarrantyTrackerMainFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class WarrantyTrackerApp {
    private static final Logger logger = LoggerFactory.getLogger(WarrantyTrackerApp.class);

    public static void main(String[] args) {
        logger.info("Starting Digital Warranty Tracker Application");
        
        // Set system properties for better GUI rendering
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        // Schedule GUI creation on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Set Look and Feel to system default
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                // Create and show the main frame
                WarrantyTrackerMainFrame mainFrame = new WarrantyTrackerMainFrame();
                
                // Show splash screen briefly
                showSplashScreen();
                
                // Center the window and make it visible
                mainFrame.setVisible(true);
                
                logger.info("Application started successfully");
                
            } catch (Exception e) {
                logger.error("Error starting application", e);
                JOptionPane.showMessageDialog(null,
                    "Error starting application: " + e.getMessage(),
                    "Startup Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
    
    private static void showSplashScreen() {
        JWindow splash = new JWindow();
        splash.setAlwaysOnTop(true);
        
        // Create splash content
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(BorderFactory.createRaisedBevelBorder());
        
        // Title
        JLabel titleLabel = new JLabel("Digital Warranty Tracker", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.BLUE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Initializing application...", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));
        
        // Features list
        String featuresText = """
            <html><center>
            <b>Features:</b><br>
            • Warranty Management<br>
            • OCR Document Scanning<br>
            • SQLite Database<br>
            • Search & Filter<br>
            • Expiration Tracking
            </center></html>
            """;
        JLabel featuresLabel = new JLabel(featuresText, JLabel.CENTER);
        featuresLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        featuresLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        content.add(titleLabel, BorderLayout.NORTH);
        content.add(subtitleLabel, BorderLayout.CENTER);
        content.add(featuresLabel, BorderLayout.SOUTH);
        
        splash.setContentPane(content);
        splash.pack();
        splash.setLocationRelativeTo(null);
        splash.setVisible(true);
        
        // Show splash for 2 seconds
        Timer timer = new Timer(2000, e -> splash.dispose());
        timer.setRepeats(false);
        timer.start();
        
        try {
            Thread.sleep(2100); // Wait for splash to close
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
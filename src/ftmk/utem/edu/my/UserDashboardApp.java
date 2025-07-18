package ftmk.utem.edu.my;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn; // NEW: Import required for styling
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

public class UserDashboardApp extends JFrame {

    private CardLayout cardLayout;
    private JPanel cardPanel;
    private DefaultTableModel foundItemsTableModel, myReportsTableModel;

    // Form fields (no changes)
    private JTextField lostItemName, lostDate, lostLocation, lostUserName, lostUserPhone;
    private JTextArea lostDescription;
    private JLabel lostImageStatusLabel;
    private File selectedLostImageFile;
    private JTextField foundItemName, foundDate, foundLocation, foundUserName, foundUserPhone;
    private JTextArea foundDescription;
    private JLabel foundImageStatusLabel;
    private File selectedFoundImageFile;

    // Constants (no changes)
    private static final Color COLOR_BACKGROUND = new Color(245, 245, 245);
    private static final Color COLOR_PRIMARY = new Color(60, 90, 153);
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 26);
    private static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_TABLE = new Font("Segoe UI", Font.PLAIN, 14);
    private static final String DASHBOARD = "Dashboard";
    private static final String REPORT_LOST = "Report Lost Item";
    private static final String VIEW_FOUND = "View Found Items";
    private static final String REPORT_FOUND = "Report Found Item";
    private static final String CHECK_STATUS = "Check Report Status";

    public UserDashboardApp() {
        super("User Dashboard - Lost & Found");
        setSize(1000, 800); // Increased size slightly for better viewing
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        cardPanel.add(createDashboardPanel(), DASHBOARD);
        cardPanel.add(createReportLostPanel(), REPORT_LOST);
        cardPanel.add(createViewFoundPanel(), VIEW_FOUND);
        cardPanel.add(createReportFoundPanel(), REPORT_FOUND);
        cardPanel.add(createCheckStatusPanel(), CHECK_STATUS);

        add(cardPanel);
    }
    
    // FIXED: This method now fetches data from the API and populates the new "Image" column.
//    private void refreshFoundItemsTable() {
//        foundItemsTableModel.setRowCount(0); 
//        try {
//            List<Item> allItems = LostItemAPIClient.fetchItems();
//            
//            List<Item> availableFoundItems = allItems.stream()
//                .filter(item -> "Found".equalsIgnoreCase(item.getFormType()) && !"Matched".equalsIgnoreCase(item.getStatus()))
//                .collect(Collectors.toList());
//
//            // Populate the table with the new image column
//            for (Item item : availableFoundItems) {
//                foundItemsTableModel.addRow(new Object[]{
//                    item.getImageUrl(), // Data for the "Image" column
//                    item.getItemName(),
//                    item.getDate().toString(),
//                    item.getLocation(),
//                    item.getDescription(),
//                    item.getUserName()
//                });
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            JOptionPane.showMessageDialog(this, "Could not load found items: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//        }
//    }
    private void loadFoundItemsAsync() {
        new SwingWorker<List<Item>, Void>() {
            @Override
            protected List<Item> doInBackground() throws Exception {
                return LostItemAPIClient.fetchItems();
            }
            @Override
            protected void done() {
                try {
                    List<Item> all = get();
                    List<Item> found = all.stream()
                        .filter(i -> "Found".equalsIgnoreCase(i.getFormType()) && !"Matched".equalsIgnoreCase(i.getStatus()))
                        .toList();

                    foundItemsTableModel.setRowCount(0);
                    for (Item item : found) {
                        foundItemsTableModel.addRow(new Object[]{
                            item.getImageUrl(), item.getItemName(), item.getDate(),
                            item.getLocation(), item.getDescription(), item.getUserName()
                        });
                        
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(UserDashboardApp.this,
                        "Error loading items: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            
        }.execute();
    }

    // FIXED: This method also fetches data and populates the new "Image" column.
//    private void refreshMyReportsTable() {
//        myReportsTableModel.setRowCount(0); 
//        try {
//            List<Item> allItems = LostItemAPIClient.fetchItems();
//            
//            List<Item> myLostReports = allItems.stream()
//                .filter(item -> "Lost".equalsIgnoreCase(item.getFormType()))
//                .collect(Collectors.toList());
//            
//            // Populate the table with the new image column
//            for (Item item : myLostReports) {
//                myReportsTableModel.addRow(new Object[]{
//                    item.getImageUrl(), // Data for the "Image" column
//                    item.getId(),
//                    item.getItemName(),
//                    item.getDate().toString(),
//                    item.getStatus()
//                });
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            JOptionPane.showMessageDialog(this, "Could not load reports: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//        }
//    }
    private void loadMyReportsAsync() {
        new SwingWorker<List<Item>, Void>() {
            @Override
            protected List<Item> doInBackground() throws Exception {
                return LostItemAPIClient.fetchItems();
            }
            @Override
            protected void done() {
                try {
                    List<Item> all = get();
                    List<Item> mine = all.stream()
                        .filter(i -> "Lost".equalsIgnoreCase(i.getFormType()))
                        .toList();

                    myReportsTableModel.setRowCount(0);
                    for (Item item : mine) {
                        myReportsTableModel.addRow(new Object[]{
                            item.getImageUrl(), item.getId(), item.getItemName(),
                            item.getDate(), item.getStatus()
                        });
                        System.out.println("Image URL: " + item.getImageUrl());
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(UserDashboardApp.this,
                        "Error loading reports: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // FIXED: This method now defines the "Image" column for the "View Found Items" panel.
    private JPanel createViewFoundPanel() {
        JPanel panel = createBasePanel(VIEW_FOUND);
        // Add "Image" as the first column
        String[] columnNames = {"Image", "Item Name", "Date Found", "Location", "Description", "Finder"};
        foundItemsTableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(foundItemsTableModel);
        styleTable(table, 80); // Set row height to 80 to make images visible
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }
    
    // FIXED: This method now defines the "Image" column for the "Check Report Status" panel.
    private JPanel createCheckStatusPanel() {
        JPanel panel = createBasePanel(CHECK_STATUS);
        // Add "Image" as the first column
        String[] columnNames = {"Image", "Report ID", "Item Name", "Date Reported", "Status"};
        myReportsTableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(myReportsTableModel);
        styleTable(table, 80); // Set row height to 80
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // FIXED: This method is updated to be identical to the one in AdminDashboardApp.
    // It now sets row height and applies the ImageRenderer.
    private void styleTable(JTable table, int rowHeight) {
        table.setFont(FONT_TABLE);
        table.setRowHeight(rowHeight);
        table.getTableHeader().setFont(FONT_BUTTON);
        table.getTableHeader().setBackground(COLOR_PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setFillsViewportHeight(true);

        // Try to find the "Image" column and apply the renderer from AdminDashboardApp
        try {
            TableColumn imageColumn = table.getColumn("Image");
            // Use the public static ImageRenderer from AdminDashboardApp
            imageColumn.setCellRenderer(new AdminDashboardApp.ImageRenderer(rowHeight));
            imageColumn.setPreferredWidth(120);
        } catch (IllegalArgumentException e) {
            // This is normal if a table doesn't have an "Image" column.
            // No action needed.
        }
    }


    // --- NO OTHER CHANGES ARE NEEDED BELOW THIS LINE ---
    // The rest of the methods are already correct.

    private void submitLostReport() {
        try {
            // Validate inputs (same as before)...

            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() {
                    return LostItemAPIClient.submitLostItem(
                        lostUserName.getText(), lostUserPhone.getText(),
                        lostItemName.getText(), lostDate.getText(),
                        lostLocation.getText(), lostDescription.getText(),
                        "Reported", "Lost", selectedLostImageFile
                    );
                }
                @Override
                protected void done() {
                    try {
                        String result = get();
                        JOptionPane.showMessageDialog(UserDashboardApp.this, result, "Info", JOptionPane.INFORMATION_MESSAGE);
                        resetLostForm();
                        cardLayout.show(cardPanel, DASHBOARD);
                        loadMyReportsAsync();
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(UserDashboardApp.this, "Submit failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Date must be YYYY-MM-DD", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void submitFoundReport() {
        try {
            // Validate inputs...

            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() {
                    return LostItemAPIClient.submitLostItem(
                        foundUserName.getText(), foundUserPhone.getText(),
                        foundItemName.getText(), foundDate.getText(),
                        foundLocation.getText(), foundDescription.getText(),
                        "Reported", "Found", selectedFoundImageFile
                    );
                }
                @Override
                protected void done() {
                    try {
                        String result = get();
                        JOptionPane.showMessageDialog(UserDashboardApp.this, "Thank you! " + result, "Success", JOptionPane.INFORMATION_MESSAGE);
                        resetFoundForm();
                        cardLayout.show(cardPanel, DASHBOARD);
                        loadFoundItemsAsync();
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(UserDashboardApp.this, "Submit failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid date: must be YYYY-MM-DD", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    private void resetLostForm() {
        lostUserName.setText(""); lostUserPhone.setText(""); lostItemName.setText("");
        lostDate.setText(""); lostLocation.setText(""); lostDescription.setText("");
        lostImageStatusLabel.setText("No image selected.");
        selectedLostImageFile = null;
    }

    private void resetFoundForm() {
        foundUserName.setText(""); foundUserPhone.setText(""); foundItemName.setText("");
        foundDate.setText(""); foundLocation.setText(""); foundDescription.setText("");
        foundImageStatusLabel.setText("No image selected.");
        selectedFoundImageFile = null;
    }
    
    private File chooseImage(JLabel statusLabel) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "png", "jpeg"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            statusLabel.setText(selectedFile.getName());
            return selectedFile;
        }
        return null;
    }

    private JPanel createReportLostPanel() {
        JPanel panel = createBasePanel(REPORT_LOST);
        lostUserName = new JTextField(20);
        lostUserPhone = new JTextField(20);
        lostItemName = new JTextField(20);
        lostDate = new JTextField(20);
        lostLocation = new JTextField(20);
        lostDescription = new JTextArea(4, 20);
        lostImageStatusLabel = new JLabel("No image selected.");

        JButton imageButton = new JButton("Insert Image");
        imageButton.addActionListener(e -> {
            selectedLostImageFile = chooseImage(lostImageStatusLabel);
        });
        
        JPanel imagePanel = new JPanel(new BorderLayout(5, 0));
        imagePanel.setOpaque(false);
        imagePanel.add(imageButton, BorderLayout.WEST);
        imagePanel.add(lostImageStatusLabel, BorderLayout.CENTER);

        JPanel form = createFormPanel(
            new String[]{"Your Name:", "Your Phone:", "Lost Item Name:", "Date Lost (YYYY-MM-DD):", "Location Lost:", "Description:", "Image (Optional):"},
            new JComponent[]{lostUserName, lostUserPhone, lostItemName, lostDate, lostLocation, new JScrollPane(lostDescription), imagePanel}
        );
        
        JButton submitButton = new JButton("Submit Report");
        submitButton.addActionListener(e -> submitLostReport());
        form.add(submitButton, createGbc(1, 7, GridBagConstraints.EAST));
        
        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createReportFoundPanel() {
        JPanel panel = createBasePanel(REPORT_FOUND);
        foundUserName = new JTextField(20);
        foundUserPhone = new JTextField(20);
        foundItemName = new JTextField(20);
        foundDate = new JTextField(20);
        foundLocation = new JTextField(20);
        foundDescription = new JTextArea(4, 20);
        foundImageStatusLabel = new JLabel("No image selected.");

        JButton imageButton = new JButton("Insert Image");
        imageButton.addActionListener(e -> {
            selectedFoundImageFile = chooseImage(foundImageStatusLabel);
        });

        JPanel imagePanel = new JPanel(new BorderLayout(5, 0));
        imagePanel.setOpaque(false);
        imagePanel.add(imageButton, BorderLayout.WEST);
        imagePanel.add(foundImageStatusLabel, BorderLayout.CENTER);

        JPanel form = createFormPanel(
            new String[]{"Your Name:", "Your Phone:", "Found Item Name:", "Date Found (YYYY-MM-DD):", "Location Found:", "Description:", "Image (Optional):"},
            new JComponent[]{foundUserName, foundUserPhone, foundItemName, foundDate, foundLocation, new JScrollPane(foundDescription), imagePanel}
        );
        
        JButton submitButton = new JButton("Submit Report");
        submitButton.addActionListener(e -> submitFoundReport());
        form.add(submitButton, createGbc(1, 7, GridBagConstraints.EAST));
        
        panel.add(form, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_BACKGROUND);
        panel.setBorder(new EmptyBorder(40, 40, 40, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("LOST & FOUND - USER DASHBOARD", SwingConstants.CENTER);
        title.setFont(FONT_TITLE);
        panel.add(title, gbc);

        gbc.insets = new Insets(20, 0, 10, 0);
        panel.add(createButton("Report a Lost Item", e -> cardLayout.show(cardPanel, REPORT_LOST)), gbc);
        panel.add(createButton("Report a Found Item", e -> cardLayout.show(cardPanel, REPORT_FOUND)), gbc);
        panel.add(createButton("View All Found Items", e -> {
            loadFoundItemsAsync();
            cardLayout.show(cardPanel, VIEW_FOUND);
        }), gbc);
        panel.add(createButton("Check My Report Status", e -> {
            loadMyReportsAsync();
            cardLayout.show(cardPanel, CHECK_STATUS);
        }), gbc);

        JButton logoutButton = createButton("Logout", e -> {
            this.dispose();
            AdminDashboardApp.main(null);
        });
        logoutButton.setBackground(new Color(220, 53, 69));
        panel.add(logoutButton, gbc);

        return panel;
    }

    private JPanel createFormPanel(String[] labels, JComponent[] fields) {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i;
            gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
            formPanel.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1;
            gbc.fill = (fields[i] instanceof JScrollPane) ? GridBagConstraints.BOTH : GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            if (fields[i] instanceof JScrollPane) gbc.weighty = 1.0; else gbc.weighty = 0;
            formPanel.add(fields[i], gbc);
        }
        return formPanel;
    }

    private GridBagConstraints createGbc(int x, int y, int anchor) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x; gbc.gridy = y; gbc.anchor = anchor;
        gbc.insets = new Insets(15, 8, 8, 8);
        return gbc;
    }

    private JButton createButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(FONT_BUTTON);
        button.setBackground(COLOR_PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(300, 50));
        button.addActionListener(action);
        return button;
    }

    private JPanel createBasePanel(String titleText) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(COLOR_BACKGROUND);
        JLabel label = new JLabel(titleText, SwingConstants.CENTER);
        label.setFont(FONT_TITLE);
        panel.add(label, BorderLayout.NORTH);
        JButton backButton = new JButton("Back to Dashboard");
        backButton.setFont(FONT_BUTTON);
        backButton.addActionListener(e -> cardLayout.show(cardPanel, DASHBOARD));
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setBackground(COLOR_BACKGROUND);
        btnPanel.add(backButton);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            UserDashboardApp userApp = new UserDashboardApp();
            userApp.setVisible(true);
        });
    }
}
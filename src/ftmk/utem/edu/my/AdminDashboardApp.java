package ftmk.utem.edu.my;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.json.JSONException;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class AdminDashboardApp extends JFrame {

    //======================================================================
    // KOD ANTARA MUKA ADMIN (ADMIN UI CODE)
    //======================================================================
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private DefaultTableModel allLostReportsModel, allFoundReportsModel, matchLostModel, matchFoundModel;
    private JTable allLostReportsTable, allFoundReportsTable, matchLostTable, matchFoundTable;
    private JTextField foundItemNameField, dateFoundField, locationFoundField;
    private JTextArea foundDescriptionArea;
    private JLabel foundImageStatusLabel;
    private File selectedFoundImageFile;

    // A list to hold all items fetched from the database
    private List<Item> allItems;

    // Constants
    private static final Color COLOR_BACKGROUND = new Color(245, 245, 245);
    private static final Color COLOR_PRIMARY = new Color(43, 87, 151);
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 26);
    private static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_TABLE = new Font("Segoe UI", Font.PLAIN, 14);

    private static final String DASHBOARD = "Dashboard";
    private static final String VIEW_REPORTS = "View All Reports";
    private static final String ADD_FOUND = "Add New Found Item";
    private static final String MATCH_ITEMS = "Match Lost & Found Items";
    
    // Custom Renderer for displaying images in a JTable
    public static class ImageRenderer extends DefaultTableCellRenderer {
        private int rowHeight;

        public ImageRenderer(int rowHeight) {
            this.rowHeight = rowHeight;
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            if (value instanceof String && ((String) value).startsWith("data:image")) {
                try {
                    String base64Data = ((String) value).split(",")[1];
                    byte[] imageBytes = Base64.getDecoder().decode(base64Data);
                    ImageIcon icon = new ImageIcon(imageBytes);
                    Image scaledImage = icon.getImage().getScaledInstance(rowHeight, rowHeight, Image.SCALE_SMOOTH);
                    icon = new ImageIcon(scaledImage);
                    return new JLabel(icon);
                } catch (Exception e) {
                    e.printStackTrace();
                    return new JLabel("Invalid image");
                }
            }

            return new JLabel("No Image");
        }
    }


    public AdminDashboardApp() {
        super("Admin Dashboard - Lost & Found");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        cardPanel.add(createDashboardPanel(), DASHBOARD);
        cardPanel.add(createViewReportsPanel(), VIEW_REPORTS);
        cardPanel.add(createAddFoundPanel(), ADD_FOUND);
        cardPanel.add(createMatchItemsPanel(), MATCH_ITEMS);

        add(cardPanel);
    }
    
    private void fetchDataFromDatabase(Runnable onDone) {
        new SwingWorker<List<Item>, Void>() {
            @Override
            protected List<Item> doInBackground() throws Exception {
            	return LostItemAPIClient.fetchItems();
            }

            @Override
            protected void done() {
                try {
                    allItems = get();
                    if (onDone != null) onDone.run();
                } catch (Exception e) {
                    allItems = null;
                    JOptionPane.showMessageDialog(AdminDashboardApp.this,
                            "Failed to fetch data: " + e.getMessage(),
                            "Connection Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        }.execute();
    }


    private JPanel createDashboardPanel() {
        // ... (No changes needed in this method)
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(COLOR_BACKGROUND);
        panel.setBorder(new EmptyBorder(40, 40, 40, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(15, 0, 15, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("LOST & FOUND - ADMIN DASHBOARD", SwingConstants.CENTER);
        title.setFont(FONT_TITLE);
        panel.add(title, gbc);

        gbc.insets = new Insets(30, 0, 15, 0);
        panel.add(createButton("View All Reports", e -> {
            refreshAllReportsTables();
            cardLayout.show(cardPanel, VIEW_REPORTS);
        }), gbc);
        panel.add(createButton("Add Found Item", e -> cardLayout.show(cardPanel, ADD_FOUND)), gbc);
        panel.add(createButton("Match Lost & Found", e -> {
            refreshMatchTables();
            cardLayout.show(cardPanel, MATCH_ITEMS);
        }), gbc);

        JButton logoutButton = createButton("Logout", e -> {
            this.dispose();
            AdminDashboardApp.main(null);
        });
        logoutButton.setBackground(new Color(220, 53, 69));
        panel.add(logoutButton, gbc);
        return panel;
    }

    private JPanel createAddFoundPanel() {
        // ... (No changes needed in this method's layout)
        JPanel panel = createBasePanel(ADD_FOUND);
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Item Name:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        foundItemNameField = new JTextField(20);
        formPanel.add(foundItemNameField, gbc);

        gbc.gridy++; gbc.gridx = 0; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Date Found (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        dateFoundField = new JTextField(20);
        dateFoundField.setText(LocalDate.now().toString());
        formPanel.add(dateFoundField, gbc);

        gbc.gridy++; gbc.gridx = 0;
        formPanel.add(new JLabel("Location Found:"), gbc);
        gbc.gridx = 1;
        locationFoundField = new JTextField(20);
        formPanel.add(locationFoundField, gbc);

        gbc.gridy++; gbc.gridx = 0;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        foundDescriptionArea = new JTextArea(4, 20);
        formPanel.add(new JScrollPane(foundDescriptionArea), gbc);

        gbc.gridy++; gbc.weighty = 0; gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        formPanel.add(new JLabel("Image (Optional):"), gbc);

        gbc.gridx = 1;
        JPanel imagePanel = new JPanel(new BorderLayout(5, 0));
        imagePanel.setOpaque(false);
        JButton imageButton = new JButton("Insert Image");
        foundImageStatusLabel = new JLabel("No image selected.");
        imageButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "png", "jpeg"));
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedFoundImageFile = fileChooser.getSelectedFile();
                foundImageStatusLabel.setText(selectedFoundImageFile.getName());
            }
        });
        imagePanel.add(imageButton, BorderLayout.WEST);
        imagePanel.add(foundImageStatusLabel, BorderLayout.CENTER);
        formPanel.add(imagePanel, gbc);

        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        JButton addButton = new JButton("Add Item to System");
        addButton.addActionListener(e -> addFoundItemByAdmin());
        formPanel.add(addButton, gbc);

        panel.add(formPanel, BorderLayout.CENTER);
        return panel;
    }

    private void addFoundItemByAdmin() {
        try {
            String name = foundItemNameField.getText();
            String dateStr = dateFoundField.getText();
            String location = locationFoundField.getText();
            LocalDate.parse(dateStr); // Validate

            if (name.isEmpty() || location.isEmpty()) {
                throw new IllegalArgumentException("Item name and location are required.");
            }

            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() {
                    return LostItemAPIClient.submitLostItem(
                            "Admin", "N/A", name, dateStr, location,
                            foundDescriptionArea.getText(),
                            "Reported", "Found", selectedFoundImageFile
                    );
                }

                @Override
                protected void done() {
                    try {
                        String result = get();
                        JOptionPane.showMessageDialog(AdminDashboardApp.this, result, "Success", JOptionPane.INFORMATION_MESSAGE);

                        // Reset form
                        foundItemNameField.setText("");
                        dateFoundField.setText(LocalDate.now().toString());
                        locationFoundField.setText("");
                        foundDescriptionArea.setText("");
                        foundImageStatusLabel.setText("No image selected.");
                        selectedFoundImageFile = null;

                        cardLayout.show(cardPanel, DASHBOARD);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(AdminDashboardApp.this, "Failed to submit item: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            }.execute();

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Please use correct date format (YYYY-MM-DD).", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to submit item: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }


    private JPanel createViewReportsPanel() {
        JPanel panel = createBasePanel(VIEW_REPORTS);

        String[] columnNames = {"ID", "Image", "Item Name", "User", "Phone", "Date", "Location", "Status"};
        allLostReportsModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        allLostReportsTable = new JTable(allLostReportsModel);

        allFoundReportsModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        allFoundReportsTable = new JTable(allFoundReportsModel);

        // Style both tables and add the image renderer
        int rowHeight = 80;
        styleTable(allLostReportsTable, rowHeight);
        styleTable(allFoundReportsTable, rowHeight);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(allLostReportsTable), new JScrollPane(allFoundReportsTable));
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(BorderFactory.createTitledBorder("Top: Lost Item Reports | Bottom: Found Item Reports"));

        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private void refreshAllReportsTables() {
        fetchDataFromDatabase(() -> {
            allLostReportsModel.setRowCount(0);
            allFoundReportsModel.setRowCount(0);

            if (allItems == null) return;

            for (Item item : allItems) {
                Object[] rowData = {
                    item.getId(),
                    item.getImageUrl(),
                    item.getItemName(),
                    item.getUserName(),
                    item.getUserPhone(),
                    item.getDate().toString(),
                    item.getLocation(),
                    item.getStatus()
                };

                if ("Lost".equalsIgnoreCase(item.getFormType())) {
                    allLostReportsModel.addRow(rowData);
                } else if ("Found".equalsIgnoreCase(item.getFormType())) {
                    allFoundReportsModel.addRow(rowData);
                }
            }
        });
    }

    // In AdminDashboardApp.java

private JPanel createMatchItemsPanel() {
    // This method call already creates a panel with a back button in the SOUTH.
    // We will overwrite it, so we need to add it back manually.
    JPanel panel = createBasePanel(MATCH_ITEMS);
    
    String[] columns = {"ID", "Item Name", "User"};
    matchLostModel = new DefaultTableModel(columns, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    matchLostTable = new JTable(matchLostModel);
    styleTable(matchLostTable, 30);

    matchFoundModel = new DefaultTableModel(columns, 0) {
         @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    matchFoundTable = new JTable(matchFoundModel);
    styleTable(matchFoundTable, 30);

    JButton matchButton = new JButton("Match Selected Items");
    matchButton.setFont(FONT_BUTTON);
    matchButton.addActionListener(e -> performMatch());

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            new JScrollPane(matchLostTable), new JScrollPane(matchFoundTable));
    splitPane.setResizeWeight(0.5);
    splitPane.setBorder(BorderFactory.createTitledBorder("Left: Unmatched Lost Items | Right: Unmatched Found Items"));
    
    panel.add(splitPane, BorderLayout.CENTER);
    
    // --- START OF FIX ---
    
    // Create a new panel for the buttons at the bottom
    JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    southPanel.setBackground(COLOR_BACKGROUND);

    // Add the "Match" button
    southPanel.add(matchButton);

    // Create a new "Back" button and add it to the same panel
    JButton backButton = new JButton("Back to Dashboard");
    backButton.setFont(FONT_BUTTON);
    backButton.addActionListener(e -> cardLayout.show(cardPanel, DASHBOARD));
    southPanel.add(backButton);
    
    // Now, add the southPanel containing BOTH buttons to the main panel.
    // This replaces the panel that had only the back button.
    panel.add(southPanel, BorderLayout.SOUTH); 
    
    // --- END OF FIX ---
    
    return panel;
}

private void refreshMatchTables() {
    fetchDataFromDatabase(() -> {
        matchLostModel.setRowCount(0);
        matchFoundModel.setRowCount(0);

        if (allItems == null) return;

        List<Item> pendingLost = allItems.stream()
                .filter(i -> "Lost".equalsIgnoreCase(i.getFormType()) && "Reported".equalsIgnoreCase(i.getStatus()))
                .collect(Collectors.toList());

        for (Item item : pendingLost) {
            matchLostModel.addRow(new Object[]{item.getId(), item.getItemName(), item.getUserName()});
        }

        List<Item> unmatchedFound = allItems.stream()
                .filter(i -> "Found".equalsIgnoreCase(i.getFormType()) && "Reported".equalsIgnoreCase(i.getStatus()))
                .collect(Collectors.toList());

        for (Item item : unmatchedFound) {
            matchFoundModel.addRow(new Object[]{item.getId(), item.getItemName(), item.getUserName()});
        }
    });
}


private void performMatch() {
    int selectedLostRow = matchLostTable.getSelectedRow();
    int selectedFoundRow = matchFoundTable.getSelectedRow();

    if (selectedLostRow == -1 || selectedFoundRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select one item from EACH list to match.", "Selection Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    int lostId = (int) matchLostModel.getValueAt(selectedLostRow, 0);
    String lostItemName = (String) matchLostModel.getValueAt(selectedLostRow, 1);
    int foundId = (int) matchFoundModel.getValueAt(selectedFoundRow, 0);
    String foundItemName = (String) matchFoundModel.getValueAt(selectedFoundRow, 1);

    int confirm = JOptionPane.showConfirmDialog(this,
            "Match Lost Item: '" + lostItemName + "' (ID: " + lostId + ")\n" +
                    "with Found Item: '" + foundItemName + "' (ID: " + foundId + ")?",
            "Confirm Match", JOptionPane.YES_NO_OPTION);

    if (confirm == JOptionPane.YES_OPTION) {
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return LostItemAPIClient.matchItemsViaAPI(lostId, foundId);
            }

            @Override
            protected void done() {
                try {
                    String result = get();
                    if (result.contains("telegram") && result.contains("sent")) {
                        JOptionPane.showMessageDialog(AdminDashboardApp.this, "Items matched and Telegram sent.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else if (result.contains("Completed")) {
                        JOptionPane.showMessageDialog(AdminDashboardApp.this, "Items matched. But no Telegram sent (user might not have started the bot).", "Partial Success", JOptionPane.WARNING_MESSAGE);
                    } else {
                        throw new IOException(result);
                    }
                    refreshMatchTables();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminDashboardApp.this, "Failed to match items: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        }.execute();
    }
}


    // Helper methods (createButton, createBasePanel, etc.)
    private void styleTable(JTable table, int rowHeight) {
        table.setFont(FONT_TABLE);
        table.setRowHeight(rowHeight);
        table.getTableHeader().setFont(FONT_BUTTON);
        table.getTableHeader().setBackground(COLOR_PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setFillsViewportHeight(true);

        // If the table has an "Image" column, set its renderer and width
        try {
            TableColumn imageColumn = table.getColumn("Image");
            imageColumn.setCellRenderer(new ImageRenderer(rowHeight));
            imageColumn.setPreferredWidth(120);
        } catch (IllegalArgumentException e) {
            // This table doesn't have an image column, do nothing.
        }
    }

    private JButton createButton(String text, ActionListener action) {
        // ... (No changes needed)
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
        // ... (No changes needed)
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));
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

    // Main method and login dialog
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) { e.printStackTrace(); }
        
        SwingUtilities.invokeLater(() -> {
            String[] options = {"User", "Admin"};
            int choice = JOptionPane.showOptionDialog(null, "Please select your role:",
                "Welcome to Lost & Found System", JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

            if (choice == 0) {
                new UserDashboardApp().setVisible(true);
            } else if (choice == 1) {
                boolean loggedIn = showAdminLoginDialog();
                if (loggedIn) {
                    new AdminDashboardApp().setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(null, "Login failed. Exiting.", "Access Denied", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                }
            } else {
                System.exit(0);
            }
        });
    }

    private static boolean showAdminLoginDialog() {
        // ... (No changes needed)
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel("Enter Admin Credentials:");
        panel.add(label, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);
        panel.add(formPanel, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(null, panel, "Admin Login",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            return username.equals("admin") && password.equals("password123");
        }
        return false;
    }
}
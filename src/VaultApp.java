import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.crypto.SecretKey;


public class VaultApp {
    private JFrame frame;
    private VaultManager vaultManager;

    public VaultApp() {
        // Initialize the vault manager before using it
    
        frame = new JFrame("Password Vault");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
    
        showLoginScreen();
    
        frame.setVisible(true);
    }
    

    private void showLoginScreen() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1));

        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = new JButton("Unlock Vault");

        panel.add(new JLabel("Enter Master Password:"));
        panel.add(passwordField);
        panel.add(loginButton);

        loginButton.addActionListener(e -> {
            String password = new String(passwordField.getPassword());
            try {
                byte[] salt = VaultManager.loadOrCreateSalt(); //load  salt (or generate one if it doesn't exist)
        
                SecretKey key = CryptoUtils.deriveKey(password, salt); //derive the secret key from the password + salt
        
                vaultManager = new VaultManager("vault.json", key, salt); //new vaultManager
                vaultManager.unlock(password); // Unlock vault (if needed)
        
                showVaultScreen();
        
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Login failed: " + ex.getMessage());
            }
        });

        frame.getContentPane().removeAll();
        frame.getContentPane().add(panel);
        frame.revalidate();
        frame.repaint();
    }

    private void showMainScreen() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Vault unlocked!")); 
        frame.getContentPane().removeAll();
        frame.getContentPane().add(panel);
        frame.revalidate();
        frame.repaint();
    }

    private void showVaultScreen() {
        frame.getContentPane().removeAll(); // clear login UI
        frame.setTitle("Vault Manager");
        frame.setLayout(new BorderLayout());
    
        // List panel
        DefaultListModel<String> siteListModel = new DefaultListModel<>();
        JList<String> siteList = new JList<>(siteListModel);
        JScrollPane scrollPane = new JScrollPane(siteList);
        frame.add(scrollPane, BorderLayout.CENTER);
    
        // Load existing sites
        for (String site : vaultManager.getSiteNames()) {
            siteListModel.addElement(site);
        }
    
        // Button panel
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Entry");
        JButton viewButton = new JButton("View Entry");
        JButton saveButton = new JButton("Save Vault");
        JButton lockButton = new JButton("Lock");
    
        buttonPanel.add(addButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(lockButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);
    
        // 🔍 View Entry button
        viewButton.addActionListener(e -> {
            String selectedSite = siteList.getSelectedValue();
            if (selectedSite != null) {
                PasswordEntry entry = vaultManager.getEntry(selectedSite);
                JOptionPane.showMessageDialog(frame,
                        "Username: " + entry.getUsername() + "\nPassword: " + entry.getPassword(),
                        "🔍 Entry for " + selectedSite,
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a site first.");
            }
        });
    
        // ➕ Add Entry button
        addButton.addActionListener(e -> {
            JTextField siteField = new JTextField();
            JTextField userField = new JTextField();
            JTextField passField = new JTextField();
            Object[] fields = {
                "Site:", siteField,
                "Username:", userField,
                "Password:", passField
            };
    
            int result = JOptionPane.showConfirmDialog(frame, fields, "➕ Add New Entry", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                PasswordEntry newEntry = new PasswordEntry(
                        siteField.getText(),
                        userField.getText(),
                        passField.getText()
                );
                vaultManager.addEntry(newEntry);
                siteListModel.addElement(newEntry.getSite());
            }
        });
    
        // 💾 Save Vault button
        saveButton.addActionListener(e -> {
            try {
                vaultManager.saveVault();
                JOptionPane.showMessageDialog(frame, "Vault saved successfully.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error saving vault: " + ex.getMessage());
            }
        });
    
        // 🔒 Lock button
        lockButton.addActionListener(e -> {
            vaultManager.lock();
            JOptionPane.showMessageDialog(frame, "Vault locked.");
            frame.dispose();
            new VaultApp(); // relaunch app
        });
    
        frame.revalidate();
        frame.repaint();
    }
    

    public static void main(String[] args) {
        // Load vault manager here before starting GUI
        SwingUtilities.invokeLater(VaultApp::new);
    }
}

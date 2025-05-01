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

    private void showVaultScreen() {
    // TODO: Replace with actual vault UI
    JOptionPane.showMessageDialog(frame, "Login successful! Vault unlocked.");
}


    private void showMainScreen() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Vault unlocked!")); 
        frame.getContentPane().removeAll();
        frame.getContentPane().add(panel);
        frame.revalidate();
        frame.repaint();
    }

    public static void main(String[] args) {
        // Load vault manager here before starting GUI
        SwingUtilities.invokeLater(VaultApp::new);
    }
}

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class VaultApp {
    private JFrame frame;
    private VaultManager vaultManager;

    public VaultApp() {
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
            boolean success = vaultManager.unlock(password);
            if (success) {
                showMainScreen();
            } else {
                JOptionPane.showMessageDialog(frame, "Incorrect password.");
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

    public static void main(String[] args) {
        // Load vault manager here before starting GUI
        SwingUtilities.invokeLater(VaultApp::new);
    }
}

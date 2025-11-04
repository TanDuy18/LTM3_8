// File: org/example/client/RegisterFrame.java
package org.example.client;

import org.example.common.BankService;

import javax.swing.*;
import java.awt.*;

public class RegisterFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmField;
    private JTextField phoneField; // <-- THÊM TRƯỜNG NÀY

    public RegisterFrame() {
        setTitle("Register New Account");
        setSize(420, 360); // Tăng chiều cao một chút
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel title = new JLabel("Register", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(title, gbc);

        // Username
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        add(new JLabel("Username:"), gbc);
        usernameField = new JTextField(15);
        gbc.gridx = 1;
        add(usernameField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Password:"), gbc);
        passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        add(passwordField, gbc);

        // Confirm Password
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(new JLabel("Confirm:"), gbc);
        confirmField = new JPasswordField(15);
        gbc.gridx = 1;
        add(confirmField, gbc);

        // Phone Number (THÊM MỚI)
        gbc.gridx = 0;
        gbc.gridy = 4;
        add(new JLabel("Phone Number:"), gbc);
        phoneField = new JTextField(15);
        phoneField.setToolTipText("Enter 10-digit phone number");
        gbc.gridx = 1;
        add(phoneField, gbc);

        // Buttons
        JButton registerBtn = new JButton("Register");
        JButton backBtn = new JButton("Back to Login");
        gbc.gridy = 5;
        gbc.gridx = 0;
        add(registerBtn, gbc);
        gbc.gridx = 1;
        add(backBtn, gbc);

        registerBtn.addActionListener(e -> performRegister());
        backBtn.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        setVisible(true);
    }

    private void performRegister() {
        String username = usernameField.getText().trim();
        String pass1 = new String(passwordField.getPassword());
        String pass2 = new String(confirmField.getPassword());
        String phone = phoneField.getText().trim();

        // Kiểm tra rỗng
        if (username.isEmpty() || pass1.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Kiểm tra mật khẩu
        if (!pass1.equals(pass2)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Kiểm tra định dạng số điện thoại: 10 chữ số
        if (!phone.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this, "Phone number must be 10 digits!", "Invalid Phone", JOptionPane.ERROR_MESSAGE);
            return;
        }

        BankService service = BankClient.getService();
        if (service == null) {
            JOptionPane.showMessageDialog(this, "Cannot connect to server.", "Connection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Boolean result = service.register(username, phone, pass1); // ĐÚNG thứ tự

            if (result != null && result) {
                JOptionPane.showMessageDialog(this,
                        "Registration successful!\nUsername: " + username + "\nPhone: " + phone,
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                new LoginFrame();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Phone number already in use!", "Registration Failed", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Server error: " + ex.getMessage(),
                    "RMI Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RegisterFrame::new);
    }
}
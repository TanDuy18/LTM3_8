// File: org/example/client/RegisterFrame.java
package org.example.client;

import org.example.common.BankService;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;

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
        String username = usernameField.getText().trim(); // Chỉ để hiển thị
        String pass1 = new String(passwordField.getPassword());
        String pass2 = new String(confirmField.getPassword());
        String sdt = phoneField.getText().trim(); // DÙNG sdt

        if (username.isEmpty() || pass1.isEmpty() || sdt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!pass1.equals(pass2)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!sdt.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this, "SĐT phải có 10 chữ số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        BankService service = BankClient.getService();
        if (service == null) {
            JOptionPane.showMessageDialog(this, "Không kết nối được server!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            boolean success = service.register(sdt, pass1); // ĐÚNG: (sdt, password)

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Đăng ký thành công!\nSĐT: " + sdt,
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                new LoginFrame();
            } else {
                JOptionPane.showMessageDialog(this,
                        "SĐT đã được sử dụng!", "Đăng ký thất bại", JOptionPane.ERROR_MESSAGE);
            }

        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi server: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RegisterFrame::new);
    }
}
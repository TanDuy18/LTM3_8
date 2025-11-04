
package org.example.client;

import org.example.common.BankService;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;

public class LoginFrame extends JFrame {
    private JTextField phoneField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;

    public LoginFrame() {
        setTitle("Bank Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("Bank Login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titleLabel, gbc);

        // SỐ ĐIỆN THOẠI
        JLabel phoneLabel = new JLabel("Số điện thoại:");
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        add(phoneLabel, gbc);

        phoneField = new JTextField(15);  // ĐÚNG: phoneField
        gbc.gridx = 1;
        add(phoneField, gbc);

        // MẬT KHẨU
        JLabel passLabel = new JLabel("Mật khẩu:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(passLabel, gbc);

        passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        add(passwordField, gbc);

        // NÚT
        loginButton = new JButton("Đăng nhập");
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(loginButton, gbc);

        registerButton = new JButton("Đăng ký");
        gbc.gridx = 1;
        add(registerButton, gbc);

        // SỰ KIỆN
        loginButton.addActionListener(e -> performLogin());
        registerButton.addActionListener(e -> openRegisterFrame());

        setVisible(true);
    }

    private void performLogin() {
        String sdt = phoneField.getText().trim();  // ĐÚNG: DÙNG phoneField
        String password = new String(passwordField.getPassword());

        if (sdt.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập SĐT và mật khẩu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
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
            int result = service.login(sdt, password);

            switch (result) {
                case 1: // Thành công
                    JOptionPane.showMessageDialog(this, "Đăng nhập thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                    new MainFrame(sdt); // Truyền sdt
                    break;

                case 0: // Sai mật khẩu
                    JOptionPane.showMessageDialog(this,
                            "SĐT hoặc mật khẩu không đúng!", "Đăng nhập thất bại", JOptionPane.ERROR_MESSAGE);
                    break;

                case -1: // Đã login ở nơi khác
                    JOptionPane.showMessageDialog(this,
                            "Tài khoản đang được đăng nhập ở thiết bị khác!\nVui lòng đăng xuất trước.",
                            "Đã đăng nhập", JOptionPane.WARNING_MESSAGE);
                    break;

                default:
                    JOptionPane.showMessageDialog(this, "Lỗi không xác định từ server.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }

        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi server: " + ex.getMessage(), "RMI Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void openRegisterFrame() {
        dispose();
        new RegisterFrame();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}
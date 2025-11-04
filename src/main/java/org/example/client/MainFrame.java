package org.example.client;

import org.example.common.BankService;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;

public class MainFrame extends JFrame {
    private String username;
    private JLabel balanceLabel;
    private String currentPhone;

    public MainFrame(String username) {
        this.username = username;
        setTitle("Bank - " + username);
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
        loadBalance();
        setVisible(true);
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // Header
        JLabel welcomeLabel = new JLabel("Welcome, " + username + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(welcomeLabel, BorderLayout.NORTH);

        // Balance
        JPanel balancePanel = new JPanel();
        balancePanel.add(new JLabel("Balance:"));
        balanceLabel = new JLabel("Loading...");
        balancePanel.add(balanceLabel);
        add(balancePanel, BorderLayout.CENTER);

        // Actions
        JPanel buttonPanel = new JPanel();
        JButton depositBtn = new JButton("Nạp tiền");
        JButton withdrawBtn = new JButton("Rút tiền");
        JButton logoutBtn = new JButton("Đăng xuất");
        JButton transferBtn = new JButton("Chuyển tiền");

        buttonPanel.add(depositBtn);
        buttonPanel.add(withdrawBtn);
        buttonPanel.add(logoutBtn);
        buttonPanel.add(transferBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        depositBtn.addActionListener(e -> deposit());
        withdrawBtn.addActionListener(e -> withdraw());
        transferBtn.addActionListener(e -> showTransferDialog());
        logoutBtn.addActionListener(e -> logout());
    }

    private void loadBalance() {
        BankService service = BankClient.getService();
        try {
            double balance = service.getBalance(username);
            balanceLabel.setText(String.format("$%.2f", balance));
        } catch (RemoteException ex) {
            balanceLabel.setText("Error");
            JOptionPane.showMessageDialog(this, "Failed to load balance.");
        }
    }

    private void deposit() {
        String amountStr = JOptionPane.showInputDialog("Enter deposit amount:");
        if (amountStr != null) {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) throw new NumberFormatException();
                BankService service = BankClient.getService();
                if (service.deposit(username, amount)) {
                    JOptionPane.showMessageDialog(this, "Deposited $" + amount);
                    loadBalance();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void withdraw() {
        String amountStr = JOptionPane.showInputDialog("Enter withdrawal amount:");
        if (amountStr != null) {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) throw new NumberFormatException();
                BankService service = BankClient.getService();
                if (service.withdraw(username, amount)) {
                    JOptionPane.showMessageDialog(this, "Withdrew $" + amount);
                    loadBalance();
                } else {
                    JOptionPane.showMessageDialog(this, "Insufficient funds or error.", "Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid amount.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void logout() {
        dispose();
        new LoginFrame();
    }
    private void showTransferDialog() {
        JDialog dialog = new JDialog(this, "Chuyển tiền", true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Số điện thoại nhận
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("SĐT người nhận:"), gbc);
        JTextField toPhoneField = new JTextField(15);
        gbc.gridx = 1;
        dialog.add(toPhoneField, gbc);

        // Số tiền
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Số tiền:"), gbc);
        JTextField amountField = new JTextField(15);
        gbc.gridx = 1;
        dialog.add(amountField, gbc);

        // Nút
        JButton confirmBtn = new JButton("Chuyển");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        dialog.add(confirmBtn, gbc);

        confirmBtn.addActionListener(e -> {
            String toPhone = toPhoneField.getText().trim();
            String amountStr = amountField.getText().trim();

            if (toPhone.isEmpty() || amountStr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) throw new Exception();

                BankService service = BankClient.getService();
                boolean success = service.transfer(currentPhone, toPhone, amount); // currentPhone = SĐT đang login

                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Chuyển tiền thành công!\n→ " + toPhone + ": $" + amount);
                    loadBalance(); // Cập nhật số dư
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Chuyển thất bại!\nKiểm tra: SĐT, số dư, hoặc trạng thái đăng nhập.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Số tiền không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }
}
// File: org/example/server/BankServiceImpl.java
package org.example.server;

import org.example.common.BankService;
import org.example.common.DBManager;
import org.example.common.SyncService;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.io.Serializable;
import java.sql.*;

public class BankServiceImpl
        extends UnicastRemoteObject
        implements BankService, Serializable {

    private static final long serialVersionUID = 1L;

    // DANH SÁCH CÁC SERVER KHÁC (THAY ĐỔI THEO MẠNG CỦA BẠN)
    private static final String[] OTHER_SERVERS = {
            "192.168.1.11:1099",
            "192.168.1.12:1099"
            // Thêm IP:port nếu có server khác
    };

    public BankServiceImpl() throws RemoteException {
        super();
    }

    // ====================== REGISTER ======================
    @Override
    public boolean register(String sdt, String password) throws RemoteException {
        String checkSql = "SELECT 1 FROM users WHERE sdt = ? LIMIT 1";
        String insertSql = "INSERT OR IGNORE INTO users (sdt, password, balance, is_logged_in) VALUES (?, ?, 0.0, FALSE)";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement check = conn.prepareStatement(checkSql);
             PreparedStatement insert = conn.prepareStatement(insertSql)) {

            check.setString(1, sdt);
            if (check.executeQuery().next()) {
                System.out.println("[REGISTER] SĐT đã tồn tại: " + sdt);
                return false;
            }

            insert.setString(1, sdt);
            insert.setString(2, password);
            insert.executeUpdate();

            System.out.println("[REGISTER] Thành công: " + sdt);
            broadcastRegister(sdt, password); // ĐỒNG BỘ

            return true;

        } catch (SQLException e) {
            throw new RemoteException("Register error", e);
        }
    }

    // ====================== LOGIN ======================
    @Override
    public int login(String sdt, String password) throws RemoteException {
        String sql = "SELECT password, is_logged_in FROM users WHERE sdt = ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, sdt);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) return 0; // Không tồn tại

            if (rs.getBoolean("is_logged_in")) {
                System.out.println("[LOGIN] Từ chối: " + sdt + " đã đăng nhập ở nơi khác.");
                return -1;
            }

            if (!rs.getString("password").equals(password)) return 0; // Sai mật khẩu

            // Cập nhật trạng thái login
            try (PreparedStatement update = conn.prepareStatement(
                    "UPDATE users SET is_logged_in = TRUE WHERE sdt = ?")) {
                update.setString(1, sdt);
                update.executeUpdate();
            }

            System.out.println("[LOGIN] Thành công: " + sdt);
            broadcastLogin(sdt); // ĐỒNG BỘ TRẠNG THÁI LOGIN
            return 1;

        } catch (SQLException e) {
            throw new RemoteException("Login error", e);
        }
    }

    // ====================== LOGOUT ======================
    @Override
    public boolean logout(String sdt) throws RemoteException {
        String sql = "UPDATE users SET is_logged_in = FALSE WHERE sdt = ? AND is_logged_in = TRUE";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, sdt);
            boolean success = pstmt.executeUpdate() > 0;

            if (success) {
                System.out.println("[LOGOUT] Thành công: " + sdt);
                broadcastLogout(sdt); // ĐỒNG BỘ TRẠNG THÁI LOGOUT
            }
            return success;

        } catch (SQLException e) {
            throw new RemoteException("Logout error", e);
        }
    }

    // ====================== BALANCE ======================
    @Override
    public double getBalance(String sdt) throws RemoteException {
        String sql = "SELECT balance FROM users WHERE sdt = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, sdt);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getDouble("balance") : 0.0;

        } catch (SQLException e) {
            throw new RemoteException("Balance error", e);
        }
    }

    // ====================== DEPOSIT ======================
    @Override
    public boolean deposit(String sdt, double amount) throws RemoteException {
        if (amount <= 0) return false;

        String sql = "UPDATE users SET balance = balance + ? WHERE sdt = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, amount);
            pstmt.setString(2, sdt);
            boolean success = pstmt.executeUpdate() > 0;

            if (success) {
                System.out.println("[DEPOSIT] " + sdt + " + " + amount);
                broadcastDeposit(sdt, amount); // ĐỒNG BỘ
            }
            return success;

        } catch (SQLException e) {
            throw new RemoteException("Deposit error", e);
        }
    }

    // ====================== WITHDRAW ======================
    @Override
    public boolean withdraw(String sdt, double amount) throws RemoteException {
        if (amount <= 0) return false;

        String checkSql = "SELECT balance FROM users WHERE sdt = ?";
        String updateSql = "UPDATE users SET balance = balance - ? WHERE sdt = ? AND balance >= ?";

        try (Connection conn = DBManager.getConnection()) {
            // Kiểm tra số dư
            try (PreparedStatement check = conn.prepareStatement(checkSql)) {
                check.setString(1, sdt);
                ResultSet rs = check.executeQuery();
                if (!rs.next() || rs.getDouble("balance") < amount) return false;
            }

            // Rút tiền
            try (PreparedStatement update = conn.prepareStatement(updateSql)) {
                update.setDouble(1, amount);
                update.setString(2, sdt);
                update.setDouble(3, amount);
                boolean success = update.executeUpdate() > 0;

                if (success) {
                    System.out.println("[WITHDRAW] " + sdt + " - " + amount);
                    broadcastWithdraw(sdt, amount); // ĐỒNG BỘ
                }
                return success;
            }

        } catch (SQLException e) {
            throw new RemoteException("Withdraw error", e);
        }
    }

    // ====================== TRANSFER ======================
    @Override
    public boolean transfer(String fromSdt, String toSdt, double amount) throws RemoteException {
        if (amount <= 0 || fromSdt.equals(toSdt)) return false;

        String checkSender = "SELECT balance, is_logged_in FROM users WHERE sdt = ?";
        String checkReceiver = "SELECT 1 FROM users WHERE sdt = ?";
        String deduct = "UPDATE users SET balance = balance - ? WHERE sdt = ? AND balance >= ?";
        String add = "UPDATE users SET balance = balance + ? WHERE sdt = ?";

        Connection conn = null;
        try {
            conn = DBManager.getConnection();
            conn.setAutoCommit(false);

            // Kiểm tra người gửi
            try (PreparedStatement ps = conn.prepareStatement(checkSender)) {
                ps.setString(1, fromSdt);
                ResultSet rs = ps.executeQuery();
                if (!rs.next() || !rs.getBoolean("is_logged_in") || rs.getDouble("balance") < amount) {
                    conn.rollback();
                    return false;
                }
            }

            // Kiểm tra người nhận
            try (PreparedStatement ps = conn.prepareStatement(checkReceiver)) {
                ps.setString(1, toSdt);
                if (!ps.executeQuery().next()) {
                    conn.rollback();
                    return false;
                }
            }

            // Trừ tiền
            try (PreparedStatement ps = conn.prepareStatement(deduct)) {
                ps.setDouble(1, amount);
                ps.setString(2, fromSdt);
                ps.setDouble(3, amount);
                if (ps.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // Cộng tiền
            try (PreparedStatement ps = conn.prepareStatement(add)) {
                ps.setDouble(1, amount);
                ps.setString(2, toSdt);
                if (ps.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            System.out.println("[TRANSFER] " + fromSdt + " → " + toSdt + " | " + amount);
            broadcastTransfer(fromSdt, toSdt, amount); // ĐỒNG BỘ

            return true;

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            throw new RemoteException("Transfer failed", e);
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }

    // ====================== BROADCAST METHODS ======================
    private void broadcastRegister(String sdt, String password) {
        broadcast(s -> s.syncRegister(sdt, password), "register");
    }

    private void broadcastLogin(String sdt) {
        broadcast(s -> s.syncLogin(sdt), "login");
    }

    private void broadcastLogout(String sdt) {
        broadcast(s -> s.syncLogout(sdt), "logout");
    }

    private void broadcastDeposit(String sdt, double amount) {
        broadcast(s -> s.syncDeposit(sdt, amount), "deposit");
    }

    private void broadcastWithdraw(String sdt, double amount) {
        broadcast(s -> s.syncWithdraw(sdt, amount), "withdraw");
    }

    private void broadcastTransfer(String fromSdt, String toSdt, double amount) {
        broadcast(s -> s.syncTransfer(fromSdt, toSdt, amount), "transfer");
    }

    @FunctionalInterface
    interface SyncAction {
        void apply(SyncService s) throws RemoteException;
    }

    private void broadcast(SyncAction action, String operation) {
        for (String addr : OTHER_SERVERS) {
            try {
                String[] parts = addr.split(":");
                Registry registry = LocateRegistry.getRegistry(parts[0], Integer.parseInt(parts[1]));
                SyncService sync = (SyncService) registry.lookup("SyncService");
                action.apply(sync);
                System.out.println("[SYNC] " + operation + " → " + addr);
            } catch (Exception e) {
                System.err.println("[SYNC] " + operation + " failed to " + addr + ": " + e.getMessage());
            }
        }
    }
}
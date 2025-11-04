// org/example/server/SyncServiceImpl.java
package org.example.server;

import org.example.common.DBManager;
import org.example.common.SyncService;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.sql.*;

public class SyncServiceImpl extends UnicastRemoteObject implements SyncService {
    public SyncServiceImpl() throws RemoteException {}

    private int executeUpdate(String sql, Object... params) {
        try (Connection conn = DBManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[SYNC] SQL Error: " + e.getMessage());
            return 0;
        }
    }

    @Override public void syncRegister(String sdt, String password) throws RemoteException {
        executeUpdate("INSERT OR IGNORE INTO users (sdt, password, balance, is_logged_in) VALUES (?, ?, 0.0, FALSE)", sdt, password);
        System.out.println("[SYNC] Registered: " + sdt);
    }

    @Override public void syncLogin(String sdt) throws RemoteException {
        executeUpdate("UPDATE users SET is_logged_in = TRUE WHERE sdt = ?", sdt);
        System.out.println("[SYNC] Login: " + sdt);
    }

    @Override public void syncLogout(String sdt) throws RemoteException {
        executeUpdate("UPDATE users SET is_logged_in = FALSE WHERE sdt = ?", sdt);
        System.out.println("[SYNC] Logout: " + sdt);
    }

    @Override public void syncDeposit(String sdt, double amount) throws RemoteException {
        executeUpdate("UPDATE users SET balance = balance + ? WHERE sdt = ?", amount, sdt);
        System.out.println("[SYNC] Deposit: " + sdt + " + " + amount);
    }

    @Override public void syncWithdraw(String sdt, double amount) throws RemoteException {
        executeUpdate("UPDATE users SET balance = balance - ? WHERE sdt = ? AND balance >= ?", amount, sdt, amount);
        System.out.println("[SYNC] Withdraw: " + sdt + " - " + amount);
    }

    @Override public void syncTransfer(String fromSdt, String toSdt, double amount) throws RemoteException {
        Connection conn = null;
        try {
            conn = DBManager.getConnection();
            conn.setAutoCommit(false);
            executeUpdate(conn, "UPDATE users SET balance = balance - ? WHERE sdt = ? AND balance >= ?", amount, fromSdt, amount);
            executeUpdate(conn, "UPDATE users SET balance = balance + ? WHERE sdt = ?", amount, toSdt);
            conn.commit();
            System.out.println("[SYNC] Transfer: " + fromSdt + " → " + toSdt);
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    private int executeUpdate(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            return ps.executeUpdate();
        }
    }
}
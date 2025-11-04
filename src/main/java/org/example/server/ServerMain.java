
package org.example.server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerMain {
    public static void main(String[] args) {
        try {
            // Tạo registry trên port 1099
            Registry registry = LocateRegistry.createRegistry(1099);
            System.out.println("RMI Registry created on port 1099");

            // Tạo đối tượng dịch vụ
            BankServiceImpl bankService = new BankServiceImpl();

            // Đăng ký dịch vụ với tên "BankService"
            registry.rebind("BankService", bankService);
            registry.rebind("SyncService", new SyncServiceImpl());
            System.out.println("BankService is running and ready...");

            while (true) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            System.err.println("Server exception:");
            e.printStackTrace();
        }
    }
}
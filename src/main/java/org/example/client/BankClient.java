package org.example.client;

import org.example.common.BankService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class BankClient {
    private static BankService bankService;

    /** Lấy service – nếu chưa có thì tạo, nếu lỗi thì ném RuntimeException */
    public static BankService getService() {
        if (bankService != null) {
            return bankService;
        }

        try {
            // 1. Kết nối tới registry (localhost:1099)
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            // 2. Lookup tên đã bind ở server
            bankService = (BankService) registry.lookup("BankService");
            System.out.println("[BankClient] Connected to BankService");
            return bankService;

        } catch (Exception e) {
            // In chi tiết để bạn biết nguyên nhân
            System.err.println("[BankClient] CANNOT CONNECT TO RMI SERVER");
            e.printStackTrace();
            return null;               // client sẽ nhận null → hiển thị thông báo
        }
    }
}

package org.example.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BankService extends Remote {

    boolean register(String sdt, String password) throws RemoteException;
    int login(String phone, String password) throws RemoteException;
    boolean logout(String phone) throws RemoteException;  // THÊM
    double getBalance(String phone) throws RemoteException;
    boolean deposit(String phone, double amount) throws RemoteException;
    boolean withdraw(String phone, double amount) throws RemoteException;

    boolean transfer(String fromPhone, String toPhone, double amount) throws RemoteException;
}

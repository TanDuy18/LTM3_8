package org.example.common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SyncService extends Remote {
    void syncRegister(String sdt, String password) throws RemoteException;
    void syncDeposit(String sdt, double amount) throws RemoteException;
    void syncWithdraw(String sdt, double amount) throws RemoteException;
    void syncTransfer(String fromSdt, String toSdt, double amount) throws RemoteException;

    void syncLogin(String sdt) throws RemoteException;
    void syncLogout(String sdt) throws RemoteException;
}

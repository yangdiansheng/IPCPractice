package com.yangdiansheng.ipc;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.widget.Toast;


public class RemoteService extends Service {

    private boolean isConnected = false;

    private Handler handler = new Handler(Looper.myLooper());

    private IConnectionService connectionService = new IConnectionService.Stub() {
        @Override
        public void connection() throws RemoteException {
            try {
                Thread.sleep(3000);
                isConnected = true;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(RemoteService.this, "connect", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void disconnection() throws RemoteException {
            isConnected  = false;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(RemoteService.this, "disconnect", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public boolean isConnection() throws RemoteException {
            return isConnected;
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return connectionService.asBinder();
    }
}

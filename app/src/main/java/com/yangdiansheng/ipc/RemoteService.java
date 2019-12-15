package com.yangdiansheng.ipc;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.yangdiansheng.ipc.entity.Message;

import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class RemoteService extends Service {

    private boolean isConnected = false;

    private Handler handler = new Handler(Looper.myLooper());

    private ArrayList<IMessageReceiveListener> messageReceiveListenerArrayList = new ArrayList<>();

    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    private ScheduledFuture scheduledFuture;

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
                scheduledFuture = scheduledThreadPoolExecutor.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        for (IMessageReceiveListener messageReceiveListener:messageReceiveListenerArrayList){
                            Message message = new Message();
                            message.setContent("this message from remote");
                            try {
                                messageReceiveListener.onRecieveMessage(message);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, 3000,3000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void disconnection() throws RemoteException {
            isConnected  = false;
            scheduledFuture.cancel(true);
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

    private IMessageService iMessageService = new IMessageService.Stub() {
        @Override
        public void sendMessage(final Message message) throws RemoteException {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(RemoteService.this, message.getContent(), Toast.LENGTH_SHORT).show();
                }
            });

            if (isConnected){
                message.setSendSuccess(true);
            } else {
                message.setSendSuccess(false);
            }
        }

        @Override
        public void registerMessageReceiveListener(IMessageReceiveListener messageReceiveListener) throws RemoteException {
            if (messageReceiveListener != null){
                messageReceiveListenerArrayList.add(messageReceiveListener);
            }
        }

        @Override
        public void unRegisterMessageReceiveListener(IMessageReceiveListener messageReceiveListener) throws RemoteException {
            if (messageReceiveListener != null){
                messageReceiveListenerArrayList.remove(messageReceiveListener);
            }
        }
    };

    private IServiceManager iServiceManager = new IServiceManager.Stub() {
        @Override
        public IBinder getService(String serviceName) throws RemoteException {
            if (IConnectionService.class.getSimpleName().equals(serviceName)){
                return connectionService.asBinder();
            }
            if (IMessageService.class.getSimpleName().equals(serviceName)){
                return iMessageService.asBinder();
            }
            return null;
        }
    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iServiceManager.asBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
    }
}

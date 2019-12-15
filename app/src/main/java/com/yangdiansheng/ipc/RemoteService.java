package com.yangdiansheng.ipc;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Messenger;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.yangdiansheng.ipc.entity.Message;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class RemoteService extends Service {

    private boolean isConnected = false;

    private Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            bundle.setClassLoader(Message.class.getClassLoader());
            Message message = bundle.getParcelable("message");
            Toast.makeText(RemoteService.this, message.getContent(), Toast.LENGTH_SHORT).show();

            Messenger clinetMessenger = msg.replyTo;
            Message reply = new Message();
            reply.setContent("message reply form remote");
            android.os.Message data = new android.os.Message();
            Bundle bundle1 = new Bundle();
            bundle1.putParcelable("data", reply);
            data.setData(bundle1);
            try {
                clinetMessenger.send(data);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }
    };

    private RemoteCallbackList<IMessageReceiveListener> messageReceiveListenerRemoteCallbackList = new RemoteCallbackList<>();

    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    private ScheduledFuture scheduledFuture;

    private Messenger messenger = new Messenger(handler);

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
                        int size =  messageReceiveListenerRemoteCallbackList.beginBroadcast();
                        for (int i = 0;i< size; i++){
                            Message message = new Message();
                            message.setContent("this message from remote");
                            try {
                                messageReceiveListenerRemoteCallbackList.getBroadcastItem(i).onRecieveMessage(message);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                        messageReceiveListenerRemoteCallbackList.finishBroadcast();
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
                messageReceiveListenerRemoteCallbackList.register(messageReceiveListener);
            }
        }

        @Override
        public void unRegisterMessageReceiveListener(IMessageReceiveListener messageReceiveListener) throws RemoteException {
            if (messageReceiveListener != null){
                messageReceiveListenerRemoteCallbackList.unregister(messageReceiveListener);
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
            if (Messenger.class.getSimpleName().equals(serviceName)){
                return messenger.getBinder();
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

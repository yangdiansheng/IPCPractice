package com.yangdiansheng.ipc;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.yangdiansheng.ipc.entity.Message;

public class MainActivity extends AppCompatActivity {

    private IConnectionService connectionServiceProxy;
    private IMessageService messageServiceProxy;
    private IServiceManager serviceManagerProxy;
    private IMessageReceiveListener messageReceiveListener = new IMessageReceiveListener.Stub() {
        @Override
        public void onRecieveMessage(final Message message) throws RemoteException {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,message.getContent(),Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.bt_connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    connectionServiceProxy.connection();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }
        });
        findViewById(R.id.bt_disconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    connectionServiceProxy.disconnection();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        findViewById(R.id.bt_is_connected).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    boolean flag = connectionServiceProxy.isConnection();
                    Toast.makeText(MainActivity.this,String.valueOf(flag),Toast.LENGTH_SHORT).show();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        findViewById(R.id.bt_send_message).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message message = new Message();
                message.setContent("message send from main");
                try {
                    messageServiceProxy.sendMessage(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        findViewById(R.id.bt_register_message).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    messageServiceProxy.registerMessageReceiveListener(messageReceiveListener);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        findViewById(R.id.bt_unregister_message).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    messageServiceProxy.unRegisterMessageReceiveListener(messageReceiveListener);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        Intent intent = new Intent(this,RemoteService.class);
        bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                serviceManagerProxy = IServiceManager.Stub.asInterface(service);
                try {
                    connectionServiceProxy = IConnectionService.Stub.asInterface(serviceManagerProxy.getService(IConnectionService.class.getSimpleName()));
                    messageServiceProxy = IMessageService.Stub.asInterface(serviceManagerProxy.getService(IMessageService.class.getSimpleName()));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, Context.BIND_AUTO_CREATE);
    }

}

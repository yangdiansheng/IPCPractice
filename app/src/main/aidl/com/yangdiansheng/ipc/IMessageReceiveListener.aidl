// IMessageReceiveListener.aidl
package com.yangdiansheng.ipc;

import com.yangdiansheng.ipc.entity.Message;


interface IMessageReceiveListener {
    void onRecieveMessage(in Message message);
}

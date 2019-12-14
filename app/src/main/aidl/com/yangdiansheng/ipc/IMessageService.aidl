// IMessageService.aidl
package com.yangdiansheng.ipc;

import com.yangdiansheng.ipc.entity.Message;
import com.yangdiansheng.ipc.IMessageReceiveListener;

//消息服务
interface IMessageService {

    //实体类要使用in或者out关键字
    void sendMessage(in Message message);

    void registerMessageReceiveListener(IMessageReceiveListener messageReceiveListener);

    void unRegisterMessageReceiveListener(IMessageReceiveListener messageReceiveListener);
}

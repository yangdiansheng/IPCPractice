// IConnectionService.aidl
package com.yangdiansheng.ipc;

//连接服务
interface IConnectionService {

      void connection();

      void disconnection();

      boolean isConnection();
}

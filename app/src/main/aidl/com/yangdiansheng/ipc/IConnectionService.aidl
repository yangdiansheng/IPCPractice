// IConnectionService.aidl
package com.yangdiansheng.ipc;

//连接服务
interface IConnectionService {

      //不能设置返回值
      oneway void connection();

      void disconnection();

      boolean isConnection();
}

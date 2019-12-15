
// IServiceManager.aidl
package com.yangdiansheng.ipc;


interface IServiceManager {

   IBinder getService(String serviceName);
}

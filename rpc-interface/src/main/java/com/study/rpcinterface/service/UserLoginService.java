package com.study.rpcinterface.service;


import com.study.rpcinterface.annotation.Rpc;
import com.study.rpcinterface.bean.RpcController;
import com.study.rpcinterface.bean.UserInfo;
import com.study.rpcinterface.bean.UserLoginInfo;

@Rpc
public interface UserLoginService extends RpcController {
     UserInfo userLogin(UserLoginInfo userLoginInfo);
}

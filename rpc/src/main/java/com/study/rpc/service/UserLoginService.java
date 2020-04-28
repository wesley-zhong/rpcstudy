package com.study.rpc.service;

import com.study.rpc.bean.UserInfo;
import com.study.rpc.bean.UserLoginInfo;
import com.study.rpc.controller.RpcController;

public interface UserLoginService extends RpcController {
     UserInfo userLogin(UserLoginInfo userLoginInfo);
}

package com.study.rpcclient.service;

import com.study.rpcinterface.bean.UserInfo;

public interface UserService   {
    UserInfo  userLogin(String userName, String password);
}

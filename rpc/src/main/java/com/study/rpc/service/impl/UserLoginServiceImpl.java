package com.study.rpc.service.impl;

import com.study.rpc.bean.UserInfo;
import com.study.rpc.bean.UserLoginInfo;
import com.study.rpc.service.UserLoginService;
import org.springframework.stereotype.Component;

@Component
public class UserLoginServiceImpl implements UserLoginService {
    public UserInfo userLogin(UserLoginInfo userLoginInfo) {
        UserInfo  userInfo = new UserInfo();
        userInfo.setUserNickName("nick_" + userLoginInfo.getUserName());
        userInfo.setUserId(100);
        return userInfo;
    }
}

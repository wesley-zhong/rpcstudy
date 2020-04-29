package com.study.rpcclient.service.UserServiceImpl;

import com.alibaba.fastjson.JSON;
import com.study.rpcclient.service.UserService;
import com.study.rpcinterface.bean.UserInfo;
import com.study.rpcinterface.bean.UserLoginInfo;
import com.study.rpcinterface.service.UserLoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserServiceImpl implements UserService, InitializingBean {
    @Autowired
    private UserLoginService userLoginService;

    @Override
    public UserInfo userLogin(String userName, String password) {
        UserLoginInfo userLoginInfo = new UserLoginInfo();
        userLoginInfo.setUserName(userName);
        userLoginInfo.setPassword(password);
        return userLoginService.userLogin(userLoginInfo);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        UserInfo userInfo = userLogin("wesley", "abc");
        log.info("########get from server  userNickName = {}",userInfo.getUserNickName());

    }

}

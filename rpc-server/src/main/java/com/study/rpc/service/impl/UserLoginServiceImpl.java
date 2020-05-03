package com.study.rpc.service.impl;


import com.alibaba.fastjson.JSON;
import com.study.rpcinterface.bean.RpcRequest;
import com.study.rpcinterface.bean.UserInfo;
import com.study.rpcinterface.bean.UserLoginInfo;
import com.study.rpcinterface.service.UserLoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserLoginServiceImpl implements UserLoginService {
    public UserInfo userLogin(UserLoginInfo userLoginInfo) {
        log.info("receive from client username ={} password={}", userLoginInfo.getUserName(), userLoginInfo.getPassword());
        UserInfo  userInfo = new UserInfo();
        userInfo.setUserNickName("nick_" + userLoginInfo.getUserName());
        userInfo.setUserId(100);
        return userInfo;
    }

    public static void main(String[] args){
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setServiceName("UserLoginService");
        rpcRequest.setMethodName("userLogin");

        //set parameter
        UserLoginInfo   userLoginInfo = new UserLoginInfo();
        userLoginInfo.setPassword("abc");
        userLoginInfo.setUserName("wesley");
        rpcRequest.setPayLoad(JSON.toJSONString(userLoginInfo));
        System.out.println(JSON.toJSONString(rpcRequest));

    }
}

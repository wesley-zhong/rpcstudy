package com.sudy.rpc.test;

import com.alibaba.fastjson.JSON;
import com.study.rpcinterface.bean.RpcRequest;
import com.study.rpcinterface.bean.UserLoginInfo;
import com.study.rpcinterface.network.HttpUtils;
import lombok.extern.slf4j.Slf4j;

import org.junit.Test;

@Slf4j
public class RpcTest {
    @Test
    public void userLoginTest() {
        UserLoginInfo  userLoginInfo = new UserLoginInfo();
        userLoginInfo.setUserName("wesley");
        userLoginInfo.setPassword("abc");

        RpcRequest  rpcRequest = new RpcRequest();
        rpcRequest.setServiceName("UserLoginService");
        rpcRequest.setMethodName("userLogin");
        rpcRequest.setPayLoad(JSON.toJSONString(userLoginInfo));
        String repsponseBody = HttpUtils.okhttpReq(rpcRequest);
        log.info("===== receive body = {}", repsponseBody);
    }
}

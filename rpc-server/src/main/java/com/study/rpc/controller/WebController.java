package com.study.rpc.controller;

import com.study.rpcinterface.exception.LogicException;
import com.study.rpc.service.impl.ServiceMgr;
import com.study.rpcinterface.bean.RpcRequest;
import com.study.rpcinterface.bean.RpcResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class WebController {
    @Autowired
    private ServiceMgr serviceMgr;

    @RequestMapping(value = "/", method = {RequestMethod.POST})
    @ResponseBody
    public RpcResponse callRpc(@RequestBody RpcRequest rpcRequest) {
        RpcResponse rpcResponse = new RpcResponse();
        try {
            Object ret = serviceMgr.callServiceMethod(rpcRequest);
            rpcResponse.setData(ret);
        } catch (LogicException e) {
            rpcResponse.setErrorCode(e.getErrorCode());
            rpcResponse.setErrorMsg(e.getMessage());
            e.printStackTrace();
        } catch (Throwable e) {
            rpcResponse.setErrorCode(-1);//unknown exception
            rpcResponse.setErrorMsg(e.getMessage());
            e.printStackTrace();
        }
        return rpcResponse;
    }
}

package com.study.rpc.service;

import com.alibaba.fastjson.JSON;
import com.study.rpc.bean.RpcRequest;
import com.study.rpc.controller.RpcController;
import com.study.rpc.exception.LogicException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

@Component
public class ServiceMgr {
    @Autowired
    private List<RpcController> rpcControllerList;

    public Object callServiceMethod(RpcRequest rpcRequest) {
        String serviceName = rpcRequest.getServiceName();
        String methodName = rpcRequest.getMethodName();

        RpcController rpcController = getServiceByName(serviceName);
        if (rpcController == null) {
            throw new LogicException(1, "service not exist");
        }
        Method method = getServiceMethod(rpcController, methodName);
        if (method == null) {
            throw new LogicException(2, "method not exist");
        }
        Type[] types = method.getGenericParameterTypes();
        Object parameter = null;
        //only process 0 or 1 args  others not process
        if (types != null && types.length == 1) {
            parameter = JSON.parseObject(rpcRequest.getPayLoad(), types[0]);
        }
        try {
            return method.invoke(rpcController, parameter);
        } catch (Exception e) {
            throw new LogicException(3, e.getMessage());
        }
    }

    private RpcController getServiceByName(String serviceName) {
        for (RpcController rpcController : rpcControllerList) {
            Class<?>[]  interfaces = rpcController.getClass().getInterfaces();
            if(interfaces == null || interfaces.length == 0){
                continue;
            }
            for(Class  interClass : interfaces){
                if(interClass.getSimpleName().equals(serviceName)){
                    return rpcController;
                }
            }
        }
        return null;
    }

    private Method getServiceMethod(RpcController rpcController, String methodName) {
        Method[] methods = rpcController.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }
}

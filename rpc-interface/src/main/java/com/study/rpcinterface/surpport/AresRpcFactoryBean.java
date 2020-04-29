package com.study.rpcinterface.surpport;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.study.rpcinterface.bean.RpcRequest;
import com.study.rpcinterface.bean.RpcResponse;
import com.study.rpcinterface.exception.LogicException;
import com.study.rpcinterface.network.HttpUtils;
import javafx.fxml.LoadException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Slf4j
public class AresRpcFactoryBean implements FactoryBean<Object>, InitializingBean,
        ApplicationContextAware {

    private Class<?> type;
    private String name;
    private ApplicationContext applicationContext;


    @Override
    public Object getObject() {
        return getTarget();
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.applicationContext = context;
    }


    Object getTarget() {
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args){
                RpcRequest rpcRequest = new RpcRequest();
                rpcRequest.setMethodName(method.getName());
                rpcRequest.setServiceName(type.getSimpleName());
                rpcRequest.setPayLoad(JSON.toJSONString(args[0]));
                String responseBody = HttpUtils.okhttpReq(rpcRequest);
                log.info("-----------  call service={} method ={}  args ={}  response ={}",
                        type.getSimpleName(), method.getName(), JSON.toJSONString(args), responseBody);

                Class<?> returnType = method.getReturnType();
                RpcResponse response = JSON.parseObject(responseBody, RpcResponse.class);
                if (response.getErrorCode() != 0) {
                    throw new LogicException(response.getErrorCode(), response.getErrorMsg());
                }
                return JSON.parseObject(JSON.toJSONString(response.getData()), returnType);
            }
        };
        return Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    @Override
    public Class<?> getObjectType() {
        return this.type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }


    @Override
    public void afterPropertiesSet() throws Exception {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

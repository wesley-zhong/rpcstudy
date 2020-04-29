package com.study.rpcinterface.bean;

import lombok.Data;

@Data
public class RpcRequest {
    private String serviceName;
    private String methodName;
    private String payLoad;//body
}

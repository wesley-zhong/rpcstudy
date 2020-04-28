package com.study.rpc.bean;

import lombok.Data;

@Data
public class RpcRequest {
    private String serviceName;
    private String methodName;
    private String payLoad;//body
}

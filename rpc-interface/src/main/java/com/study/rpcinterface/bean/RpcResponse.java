package com.study.rpcinterface.bean;

import lombok.Data;

@Data
public class RpcResponse {
    private int errorCode;
    private String errorMsg;
    private Object data;
}

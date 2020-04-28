package com.study.rpc.bean;

import lombok.Data;

@Data
public class RpcResponse {
    private int errorCode;
    private String errorMsg;
    private Object data;
}

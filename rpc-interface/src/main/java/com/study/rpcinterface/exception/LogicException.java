package com.study.rpcinterface.exception;

import lombok.Data;

@Data
public class LogicException extends RuntimeException {
    private int errorCode;

    public LogicException(int errCode, String msg) {
        super(msg);
        this.errorCode = errCode;
    }
}

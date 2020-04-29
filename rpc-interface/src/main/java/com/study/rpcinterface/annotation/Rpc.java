package com.study.rpcinterface.annotation;

public @interface Rpc {
    boolean primary() default true;
}

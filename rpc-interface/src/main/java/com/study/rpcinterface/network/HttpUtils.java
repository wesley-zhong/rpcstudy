package com.study.rpcinterface.network;

import com.alibaba.fastjson.JSON;
import com.study.rpcinterface.exception.LogicException;
import okhttp3.*;
import okio.BufferedSink;


import javax.security.auth.login.LoginException;
import java.io.IOException;

public class HttpUtils {
    private static String RPC_URL = "http://localhost:8080";
    private static final OkHttpClient client = new OkHttpClient();

    public static String okhttpReq(Object requestBody) {
        RequestBody requestBodysend = new RequestBody() {
            @Override
            public MediaType contentType() {
                return MediaType.parse("application/json");
            }

            @Override
            public void writeTo(BufferedSink bufferedSink) throws IOException {
                bufferedSink.writeUtf8(JSON.toJSONString(requestBody));
            }
        };
        Request request = new Request.Builder()
                .url(RPC_URL)
                .post(requestBodysend)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            }
        } catch (IOException e) {
          throw  new LogicException(100, "网络错误");
        }
        throw  new LogicException(101, "未知错误");
    }
}

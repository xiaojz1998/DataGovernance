package com.atguigu.dga.util;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class HttpUtil {
    private static OkHttpClient httpClient = new OkHttpClient();

    public static String get(String url){
        try {
            // HttpClient 、 OKHttp .....
            Request request = new Request.Builder()
                    .url(url)
                    .get()  // 是get 请求
                    .build();
            Call call = httpClient.newCall(request);
            Response response = call.execute();

            String result = response.body().string();
            return result;

        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
    public static void main(String[] args) {
        String result = get("http://hadoop102:18080/api/v1/applications/application_1684083580862_0012");
        System.out.println(result);
    }
}

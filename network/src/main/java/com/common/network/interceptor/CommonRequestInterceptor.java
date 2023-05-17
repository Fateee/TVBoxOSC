package com.common.network.interceptor;

import android.os.Build;
import android.text.TextUtils;

//import com.common.baselib.BaseApplication;
//import com.common.baselib.constant.Constant;
//import com.common.baselib.token.TokenManger;
import com.common.network.base.INetworkRequiredInfo;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class CommonRequestInterceptor implements Interceptor {
    private INetworkRequiredInfo requiredInfo;

    public CommonRequestInterceptor(INetworkRequiredInfo requiredInfo) {
        this.requiredInfo = requiredInfo;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
//        builder.addHeader("platform","android");
//        builder.addHeader("appver",requiredInfo != null ? requiredInfo.getAppVersionName():"");
////        String userStr = TokenManger.getUserStr();
//        String userToken = TokenManger.getToken();
////        builder.addHeader("userStr", TextUtils.isEmpty(userStr)?"":userStr);
//        builder.addHeader("token",TextUtils.isEmpty(userToken)?"":userToken);
//        builder.addHeader("channel", BaseApplication.CHANNEL);
//        builder.addHeader("model", Build.MODEL);
//        builder.addHeader("brand", Build.BOARD);
//        builder.addHeader("manufacturer",Build.MANUFACTURER);
//        builder.addHeader("mid", Constant.DEVICE_ID);
        return chain.proceed(builder.build());
    }
}

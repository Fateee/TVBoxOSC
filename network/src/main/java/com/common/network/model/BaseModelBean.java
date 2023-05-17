package com.common.network.model;

import android.text.TextUtils;


import com.alibaba.fastjson.JSON;

import java.io.Serializable;
import java.util.ArrayList;

public class BaseModelBean implements Serializable {
    public long updateTimeMills;
    public int errno;
    public String msg;
    public String env;
    public String serverDt;
    public String processTime;
    public String data;
    public Object bodyData;
    public Throwable error;
    public String scheme;

    public <T> Object parseObject(Class<T> cls,boolean isArray) {
        try {
            if (!TextUtils.isEmpty(data) && !"[]".equals(data) && !"{}".equals(data) && cls != null) {
                if (data.startsWith("{")) {
                    bodyData = JSON.parseObject(data, cls);
                } else if (data.startsWith("[")){
                    bodyData = JSON.parseArray(data, cls);
                }
            }
            if (bodyData == null) {
                if (isArray) {
                    return new ArrayList<T>();
                } else {
                    bodyData = cls != null ? cls.newInstance() : null;
                }
            }
            if (bodyData != null) {
                if (bodyData instanceof BaseModelBean) {
                    ((BaseModelBean) bodyData).data = data;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bodyData;
    }
}

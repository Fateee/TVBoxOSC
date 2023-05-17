package com.common.network.model;

public interface IBaseModelListener {
//    void onLoadFinish(MvvmBaseModel model, T data, PagingResult... results);
    void onLoadFinish(MvvmBaseModel model, PagingResult... results);
    void onLoadFail(MvvmBaseModel model,String prompt, PagingResult... results);

}

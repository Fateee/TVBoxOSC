package com.common.network.observer;

import android.util.Log;

import com.common.network.errorhandler.ExceptionHandler;
import com.common.network.exception.ResponseThrowable;
import com.common.network.model.MvvmBaseModel;
import com.common.network.model.MvvmNetworkObserver;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

public class BaseObserver<T> implements Observer<T> {
    MvvmBaseModel<T> baseModel;
    protected MvvmNetworkObserver<T> mvvmNetworkObserver;
    private static final String TAG = "BaseObserver";

    public BaseObserver(MvvmBaseModel<T> baseModel, MvvmNetworkObserver<T> mvvmNetworkObserver) {
        this.baseModel = baseModel;
        this.mvvmNetworkObserver = mvvmNetworkObserver;
    }

    @Override
    public void onSubscribe(@NonNull Disposable d) {
        if (baseModel != null) {
            baseModel.addDisposable(d);
        }
    }

    @Override
    public void onNext(@NonNull T t) {
        if (mvvmNetworkObserver != null) {
            mvvmNetworkObserver.onSuccess(t,false);
        }
    }

    @Override
    public void onError(@NonNull Throwable e) {
        Log.e(TAG,"onError : "+e.getMessage());
        if (mvvmNetworkObserver != null) {
            if (e instanceof ResponseThrowable) {
                mvvmNetworkObserver.onFailure(e);
            } else {
                mvvmNetworkObserver.onFailure(new ResponseThrowable(e, ExceptionHandler.ERROR.UNKNOWN));
            }
        }
    }

    @Override
    public void onComplete() {

    }
}

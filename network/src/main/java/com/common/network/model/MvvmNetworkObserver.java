package com.common.network.model;

public interface MvvmNetworkObserver<F> {
    void onSuccess(F data, boolean isFromCache);

    void onFailure(Throwable e);
}

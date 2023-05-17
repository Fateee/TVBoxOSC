package com.common.network.model;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public abstract class MvvmBaseModel<F> implements MvvmNetworkObserver<F> {
    private CompositeDisposable compositeDisposable;
    protected ReferenceQueue<IBaseModelListener> mReferenceQueue;
    protected ConcurrentLinkedQueue<WeakReference<IBaseModelListener>> mWeakListenerArrayList;
    private final BaseModelBean mResData;
    public int pageNumber = 0;
    private final String mCachedSpKey;
    private final String mApkPreloadData;
    private final boolean mIsPaging;
    private final Class<?> clazz;
    public int INIT_PAGE_NUMBER;
    public static final int TYPE_OBJECT = 0;//对象类型
    public static final int TYPE_ARRAY = 1;//数组对象类型
    private final int responseType;

    public MvvmBaseModel() {
        this(null,TYPE_OBJECT,null,null,false);
    }

    public MvvmBaseModel(boolean mIsPaging) {
        this(null,TYPE_OBJECT,null,null,mIsPaging);
    }

    public MvvmBaseModel(Class<?> clazz,String preloadData) {
        this(clazz,TYPE_OBJECT,null,preloadData,false);
    }

    public MvvmBaseModel(Class<?> clazz,int responseType,String preloadData) {
        this(clazz,responseType,null,preloadData,false);
    }

    public MvvmBaseModel(Class<?> clazz,int responseType, String mCachedSpKey, String mApkPreloadData, boolean mIsPaging, int... pageNumber) {
        this.mCachedSpKey = mCachedSpKey;
        this.mApkPreloadData = mApkPreloadData;
        this. mIsPaging = mIsPaging;
        this.INIT_PAGE_NUMBER = (pageNumber != null && pageNumber.length > 0) ? pageNumber[0] : 1;
        this.pageNumber = INIT_PAGE_NUMBER;
        mReferenceQueue = new ReferenceQueue<>();
        mWeakListenerArrayList = new ConcurrentLinkedQueue<>();
        mResData = new BaseModelBean();
        this.clazz = clazz;
        this.responseType = responseType;
    }

    public boolean isPaging() {
        return mIsPaging;
    }

    public void register(IBaseModelListener listener) {
        if (listener == null) return;
        synchronized (this) {
            //每次注册的时候清理已经被系统回收的对象
            Reference<? extends IBaseModelListener> releaseListener;
            while ((releaseListener = mReferenceQueue.poll()) != null) {
                mWeakListenerArrayList.remove(releaseListener);
            }
            for (WeakReference<IBaseModelListener> weakListener : mWeakListenerArrayList) {
                IBaseModelListener listenerItem = weakListener.get();
                if (listenerItem == listener) return;
            }
            WeakReference<IBaseModelListener> weakListener = new WeakReference<>(listener, mReferenceQueue);
            mWeakListenerArrayList.add(weakListener);
        }
    }

    public void unregister(IBaseModelListener listener) {
        if (listener == null) return;
        synchronized (this) {
            for (WeakReference<IBaseModelListener> weakListener : mWeakListenerArrayList) {
                IBaseModelListener listenerItem = weakListener.get();
                if (listenerItem == listener) {
                    mWeakListenerArrayList.remove(weakListener);
                    break;
                }
            }
        }
    }

    //为保证app打开的时候由于网络慢或者异常的情况下界面为空，所以app对渠道数据进行预置，加载完成以后会立即进行网络请求，同时缓存在本地，
    //今后app打开都会从sp去取，不再读取预置数据，由于渠道数据变化没那么快，在app第二次打开的时候会生效，并且一天请求一次
//    protected void saveDataToSp(F data) {
//        if (data != null) {
//            mResData.bodyData = data;
//            mResData.updateTimeMills = System.currentTimeMillis();
////            SpUtil.getInstance().setString(mCachedSpKey,new Gson().toJson(mResData));
//        }
//    }

    public void refresh() {
        if (isPaging()) {
            pageNumber = INIT_PAGE_NUMBER;
        }
        load();
    }
    public abstract void load();

    //是否更新数据，可以在这里设计策略，比如一天一次，一月一次等等，默认每次请求都更新
    protected boolean isNeedToUpdate() {
        return true;
    }

    //取消请求
    public void cancel() {
        if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
    }

    public void addDisposable(Disposable d) {
        if (d == null) return;
        if (compositeDisposable == null) {
            compositeDisposable = new CompositeDisposable();
        }
        compositeDisposable.add(d);
    }

    public void getCachedDataAndLoad() {
        if (mCachedSpKey != null) {
//            String savedStr = SpUtil.getInstance().getString(mCachedSpKey);
//            if (!TextUtils.isEmpty(savedStr)) {
//                try {
//                    F savedData = new Gson().fromJson(new JSONObject(savedStr).getString("data"),clazz);
//                    if (savedData != null) {
//                        onSuccess(savedData,true);
//                        if (isNeedToUpdate()) {
//                            load();
//                        }
//                        return;
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
        }
        if (!TextUtils.isEmpty(mApkPreloadData)) {
            F saveData;
            switch (responseType) {
                case TYPE_ARRAY:
                    saveData = (F) JSON.parseArray(mApkPreloadData,clazz);
                    break;
                default:
                    saveData = (F) JSON.parseObject(mApkPreloadData,clazz);
                    break;
            }
            if (saveData != null) {
                onSuccess(saveData, true);
            }
        }
        load();
    }

    public void notifyResultToListener(Object netResBean) {
        notifyResultToListener(netResBean,false);
    }

    //发送消息个ui线程
    protected void notifyResultToListener(Object netResBean,boolean isFromCache) {
        mResData.bodyData = netResBean;
        synchronized (this) {
            for (WeakReference<IBaseModelListener> weakListener : mWeakListenerArrayList) {
                IBaseModelListener listenerItem = weakListener.get();
                if (listenerItem != null) {
                    if (isPaging()) {
                        boolean isEmpty = netResBean == null;
                        if (!isEmpty) {
                            if (netResBean instanceof List) {
                                isEmpty = ((List) netResBean).isEmpty();
                            }
                        }
                        listenerItem.onLoadFinish(this, !isFromCache ? new PagingResult(pageNumber == INIT_PAGE_NUMBER, isEmpty,
                                ((netResBean instanceof List) && ((List) netResBean).size() > 0))
                                : new PagingResult(true, false, true));
                    } else {
                        listenerItem.onLoadFinish(this);
                    }
                }
            }
            if (isPaging()) {
                if (mCachedSpKey != null && pageNumber == INIT_PAGE_NUMBER && !isFromCache) {
//                    saveDataToSp(netResBean);
                }
                if (!isFromCache) {
                    if (netResBean != null && netResBean instanceof List && ((List) netResBean).size() > 0) {
                        pageNumber ++;
                    }
                }
            } else {
                if (mCachedSpKey != null && !isFromCache) {
//                    saveDataToSp(netResBean);
                }
            }
        }
    }

    public BaseModelBean getResData() {
        return mResData;
    }

    protected void loadFail(Throwable e) {
        mResData.error = e;
        String errMsg =  e.getMessage() != null ? e.getMessage() : e.toString();
        synchronized (this) {
            for (WeakReference<IBaseModelListener> weakListener : mWeakListenerArrayList) {
                IBaseModelListener listenerItem = weakListener.get();
                if (listenerItem != null) {
                    if (isPaging()) {
                        listenerItem.onLoadFail(this,errMsg, new PagingResult(true,pageNumber == INIT_PAGE_NUMBER,false));
                    } else {
                        listenerItem.onLoadFail(this,errMsg);
                    }
                }
            }
        }
    }
}

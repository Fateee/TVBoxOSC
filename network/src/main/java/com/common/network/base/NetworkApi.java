package com.common.network.base;

import com.common.network.environment.IEnvironment;
import com.common.network.errorhandler.ExceptionHandler;
import com.common.network.errorhandler.HttpErrorHandler;
import com.common.network.interceptor.CommonRequestInterceptor;
import com.common.network.interceptor.CommonResponseInterceptor;
import com.common.network.model.BaseModelBean;
import com.common.network.observer.BaseObserver;
//import com.common.network.util.NetUtils;

//import org.jetbrains.annotations.NotNull;

import java.net.ConnectException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.fastjson.FastJsonConverterFactory;


public abstract class NetworkApi implements IEnvironment {
    private static INetworkRequiredInfo iNetworkRequiredInfo;
    private static final HashMap<String,Retrofit> retrofitHashMap = new HashMap<>();
    private static final boolean mIsOnLine = true;
    private OkHttpClient mOkHttpClient;
    private String mBaseUrl;

    protected NetworkApi() {
        if (!mIsOnLine) {
            mBaseUrl = getTest();
        }
        mBaseUrl = getOnline();
    }

    public static void init(INetworkRequiredInfo networkRequiredInfo) {
        iNetworkRequiredInfo = networkRequiredInfo;
        //获取sp里存贮的当前是正式还是测试环境的变量值
//        mIsOnLine =
    }

    protected Retrofit getRetrofit(Class service) {
        if (retrofitHashMap.get(mBaseUrl + service.getName()) != null) {
            return retrofitHashMap.get(mBaseUrl + service.getName());
        }
        Retrofit.Builder builder = new Retrofit.Builder();
        builder.baseUrl(mBaseUrl);
        builder.client(getOkHttpClient());
        builder.addConverterFactory(FastJsonConverterFactory.create());
        builder.addCallAdapterFactory(RxJava2CallAdapterFactory.create());
        Retrofit retrofit = builder.build();
        retrofitHashMap.put(mBaseUrl + service.getName(),retrofit);
        return retrofit;
    }

    private OkHttpClient getOkHttpClient() {
        if (mOkHttpClient == null) {
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
            if (getInterceptor() != null) {
                clientBuilder.addInterceptor(getInterceptor());
            }
            clientBuilder.addInterceptor(new CommonRequestInterceptor(iNetworkRequiredInfo));
            clientBuilder.addInterceptor(new CommonResponseInterceptor());
            if (iNetworkRequiredInfo != null && (iNetworkRequiredInfo.isDebug())) {
                HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
                httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                clientBuilder.addInterceptor(httpLoggingInterceptor);
            }
//            if (isHttpsCaptureEnabled()) {
//                clientBuilder.sslSocketFactory(buildSslSocketFactory(), new TrustAllManager());
//            }
            mOkHttpClient = clientBuilder.build();
        }
        return mOkHttpClient;
    }

    public <T> ObservableTransformer<T,T> applySchedules(final Observer<T> observer) {
        return upstream -> {
            Observable<T> observable = upstream.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).map(getAppErrorHandler()).onErrorResumeNext(new HttpErrorHandler<T>());
            observable.subscribe(observer);
            return observable;
        };
    }

    protected abstract Interceptor getInterceptor();

    protected abstract <T> Function<T, T> getAppErrorHandler();

    public <T> void subscribe(Observable<BaseModelBean> observable, final BaseObserver<T> observer, Class cls, boolean isArray) {
//        if (!NetUtils.isNetworkConnected()) {
//            observer.onError(ExceptionHandler.handleException(new ConnectException("network error")));
//            return;
//        }
        observable.compose(upstream ->
                upstream.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(getAppErrorHandler())
                        .onErrorResumeNext(new HttpErrorHandler<BaseModelBean>()))
                .map(res -> (T) res.parseObject(cls,isArray)).subscribe(observer);

        //without lamda
//        observable.compose(new ObservableTransformer<BaseResponseEntity<T>, BaseResponseEntity<T>>() {
//            @NonNull
//            @Override
//            public ObservableSource<BaseResponseEntity<T>> apply(@NonNull Observable<BaseResponseEntity<T>> upstream) {
//                Observable<BaseResponseEntity<T>> observabl = upstream.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).map(getAppErrorHandler()).onErrorResumeNext(new HttpErrorHandler<>());
//                return observabl;
//            }
//        }).map(new Function<BaseResponseEntity<T>, T>() {
//            @Override
//            public T apply(@NonNull BaseResponseEntity<T> res) throws Exception {
//                return res.parseObject(cls);
//            }
//        }).subscribe(observer);
    }

//    /**
//     * 是否允许抓包
//     *
//     * @return
//     */
//    private static boolean isHttpsCaptureEnabled() {
//        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.N)return true;
//        //开发者选项
//        return (boolean) SpUtil.getInstace().getBoolean(KEY_HTTP_CAPTURE, BuildConfig.DEBUG);
//    }

    public static SSLSocketFactory buildSslSocketFactory() {
        SSLSocketFactory sSLSocketFactory = null;
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new TrustAllManager()}, new SecureRandom());
            sSLSocketFactory = sslContext.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sSLSocketFactory;
    }

    public static class TrustAllManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    /**
     * 刷新okhttpclient以及retrofit
     */
    public void refreshClient() {
        retrofitHashMap.clear();
        mOkHttpClient = null;
    }

}

package study.strengthen.china.tv.config;

import android.util.Log
import com.common.network.base.NetworkApi
import com.common.network.model.BaseModelBean
import com.common.network.model.MvvmNetworkObserver
import com.common.network.observer.BaseObserver
import com.orhanobut.hawk.Hawk
import io.reactivex.Observable
import io.reactivex.functions.Function
import study.strengthen.china.tv.util.HawkConfig
import java.util.ArrayList

class AppConfigApi private constructor() : NetworkApi(){

    companion object {
        private const val TAG = "TestApi"
        const val STATISTICS_HOST = "https://gitee.com/" //"https://gitlab.com/" //"https://gitee.com/"//https://raw.githubusercontent.com/Fateee/myconfig/main/shantech.json
        val instance = Holder.holder
//        private val deviceId : String? = PushServiceFactory.getCloudPushService().deviceId

        fun <T> getService(service : Class<T>) : T {
            return instance.getRetrofit(service).create(service)
        }

        fun <T> subscribe(request: Observable<BaseModelBean>, observer: BaseObserver<T>, clazz : Class<*> = BaseModelBean::class.java, isArray : Boolean = false) {
            instance.subscribe(request,observer,clazz,isArray)
        }

//        @JvmStatic
//        fun test(body: Map<String, String>? = null, needNetwork : Boolean = true) {
//            val request = getService(StatisticsApiList::class.java).test()
//            val observer = SaObserver(null, object : MvvmNetworkObserver<Response<String>> {
//                override fun onSuccess(data: Response<String>, isFromCache: Boolean) {
//                    Log.e(TAG,"track onSuccess   "+data.body())
//                }
//
//                override fun onFailure(e: Throwable?) {
//                    Log.i(TAG,"track onFailure")
//                }
//            })
//            if (needNetwork && !NetUtil.isNetworkConnected()) {
//                observer.onError(ExceptionHandler.handleException(ConnectException("network error")))
//                return
//            }
//            request.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(observer)
//        }

        @JvmStatic
        fun getAppConfig(cb: (Boolean, Int?, UpdateAppBean?) -> Unit) {
            val observer = BaseObserver<BaseModelBean>(null, object : MvvmNetworkObserver<BaseModelBean> {
                override fun onSuccess(data: BaseModelBean?, isFromCache: Boolean) {
                    val appConfigBean = data as AppConfig?
                    Log.i(TAG,"getAppConfig   "+appConfigBean?.toString())
                    appConfigBean?.let {
                        AppInfo.mAddress = it.appInfo?.address
                        val history = Hawk.get(HawkConfig.API_HISTORY, ArrayList<String>())
                        if (AppInfo.mAddress != null && !history.contains(AppInfo.mAddress!!)) history.add(0, AppInfo.mAddress!!)
                        if (history.size > 10) history.removeAt(10)
                        Hawk.put(HawkConfig.API_HISTORY, history)
                        val current = Hawk.get(HawkConfig.API_URL, "")
                        if (current.isNullOrEmpty() || it.appInfo?.forceUrl == true) {
                            Hawk.put(HawkConfig.API_URL, AppInfo.mAddress)
                        }
                        AppInfo.mRec = it.appInfo?.rec ?: 0
                    }
//                    val showSplashAd = appConfigBean?.adInfo?.type !=0 && appConfigBean?.adInfo?.open_screen?:false //开屏是否展示ad
                    cb.invoke(false, appConfigBean?.appInfo?.intent, appConfigBean?.updateInfo)
                }

                override fun onFailure(e: Throwable?) {
                    Log.i(TAG,"getAdConfig  error "+e.toString())
                    cb.invoke(false,1,null)
                }
            })
            val request = getService(StatisticsApiList::class.java).getAdControl()
            subscribe(request,observer, AppConfig::class.java)
        }

    }

    private object Holder {
        val holder = AppConfigApi()
    }

    override fun <T : Any?> getAppErrorHandler(): Function<T, T> {
        return Function {response ->
            return@Function response
        }
    }

    override fun getInterceptor()= null

    override fun getTest() = STATISTICS_HOST

    override fun getOnline() = STATISTICS_HOST
}
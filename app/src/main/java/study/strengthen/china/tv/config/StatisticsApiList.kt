package study.strengthen.china.tv.config

import com.common.network.model.BaseModelBean
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface StatisticsApiList {

    @GET("dot")
    fun statistics(@QueryMap map: Map<String, String>?) : Observable<Response<String>>

//    http://cdn-ali-file-digbird.shanhutech.cn/digbird/headimg/2022101505/aaaaaa.json
//    @GET("fateee/test-config/raw/master/shantech.json")
//    fun test() : Observable<Response<String>>
//
//    @GET("fateee/test-config/raw/master/mytest.json")https://gitee.com/fateee/test-config/raw/master/adconfig.json
//    fun getAdControl() : Observable<BaseModelBean>

    @GET("Fateee/testconfig/-/raw/main/shantech.json")
    fun test() : Observable<Response<String>>

    @GET("fateee/test-config/raw/master/appconfig.json")
    fun getAdControl() : Observable<BaseModelBean>

    @POST(" mb/mat/reco")
    fun reco(@Body  body : RequestBody) : Observable<Response<String>>
}

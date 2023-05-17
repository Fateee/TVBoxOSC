package study.strengthen.china.tv.config

import com.common.network.model.BaseModelBean


class AppConfig : BaseModelBean(){
    var appInfo: AppInfoBean? = null
    var updateInfo: UpdateAppBean? = null

    class AppInfoBean : BaseModelBean() {
        var intent = 0//0-dead 1-paper 2-video
        var address: String? = null
        var secret: String? = null
        var rec = 1
        var forceUrl = false
        override fun toString(): String {
            return "AppInfoBean(intent=$intent, address=$address, secret=$secret, rec=$rec)"
        }

    }

    override fun toString(): String {
        return "AppConfig(appInfo=${appInfo.toString()}, updateInfo=${updateInfo.toString()})"
    }
}
package study.strengthen.china.tv.config

import com.common.network.model.BaseModelBean


open class UpdateAppBean : BaseModelBean(){
    var appver: String? = null
    var is_force_update = false
    var update_content: String? = null
    var tanchuan_rate = 0
    var tanchuan_limit = 0
    var install_file: String? = null
    var file_md5: String? = null
    override fun toString(): String {
        return "UpdateAppBean(appver=$appver, is_force_update=$is_force_update, update_content=$update_content, tanchuan_rate=$tanchuan_rate, tanchuan_limit=$tanchuan_limit, install_file=$install_file, file_md5=$file_md5)"
    }


}
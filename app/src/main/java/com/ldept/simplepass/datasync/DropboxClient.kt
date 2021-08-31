package com.ldept.simplepass.datasync

import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2

class DropboxClient {

    fun getClient(accessToken : String) : DbxClientV2{
        val config = DbxRequestConfig("SimplePass")
        return DbxClientV2(config, accessToken)
    }

}
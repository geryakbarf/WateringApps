package com.id.wtf.wateringapps.api

import com.android.volley.Response
import com.android.volley.toolbox.StringRequest

class JadwalRequest(jadwal: String, listener: Response.Listener<String>) :
    StringRequest(Method.POST, URL, listener, null) {

    private val params: MutableMap<String, String>

    init {
        params = HashMap()
        params.put("jadwal", jadwal)
    }

    override fun getParams(): MutableMap<String, String> {
        return params
    }

    companion object {
        val URL = "http://iot.alwiyahyamuljabar.xyz/Android/jadwal.php"
    }
}
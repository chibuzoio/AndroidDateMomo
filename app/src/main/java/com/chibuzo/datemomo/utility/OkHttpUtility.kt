package com.chibuzo.datemomo.utility

import android.content.Context
import android.content.SharedPreferences
import com.chibuzo.datemomo.R
import com.chibuzo.datemomo.model.request.UpdateActiveStatusRequest
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.*
import java.io.IOException

class OkHttpUtility {

    companion object {

        @Throws(IOException::class)
        fun updateUserStatus(context: Context,
                             sharedPreferences: SharedPreferences, isActivityActive: Boolean) {
            val mapper = jacksonObjectMapper()
            val updateActiveStatusRequest = UpdateActiveStatusRequest(
                sharedPreferences.getInt(context.getString(R.string.member_id), 0),
                if (isActivityActive) 1 else 0)

            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

            val jsonObjectString = mapper.writeValueAsString(updateActiveStatusRequest)
            val requestBody: RequestBody = RequestBody.create(
                MediaType.parse("application/json"),
                jsonObjectString
            )

            val client = OkHttpClient()
            val request: Request = Request.Builder()
                .url(context.getString(R.string.date_momo_api) + context.getString(R.string.api_user_status_update))
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    call.cancel()
                }

                override fun onResponse(call: Call, response: Response) {
                    val myResponse: String = response.body()!!.string()
                }
            })
        }
    }
}



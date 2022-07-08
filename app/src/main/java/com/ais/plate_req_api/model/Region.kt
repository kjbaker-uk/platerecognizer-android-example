package com.ais.plate_req_api.model

import com.google.gson.annotations.SerializedName

data class Region(

    @SerializedName("code") var code: String? = null,
    @SerializedName("score") var score: Double? = null

)

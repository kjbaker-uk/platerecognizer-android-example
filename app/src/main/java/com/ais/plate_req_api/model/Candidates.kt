package com.ais.plate_req_api.model

import com.google.gson.annotations.SerializedName

data class Candidates(

    @SerializedName("score") var score: Double? = null,
    @SerializedName("plate") var plate: String? = null

)

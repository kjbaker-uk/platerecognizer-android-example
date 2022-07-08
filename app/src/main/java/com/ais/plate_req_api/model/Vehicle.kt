package com.ais.plate_req_api.model

import com.google.gson.annotations.SerializedName

data class Vehicle(

    @SerializedName("score") var score: Double? = null,
    @SerializedName("type") var type: String? = null,
    @SerializedName("box") var box: Box? = Box()


)
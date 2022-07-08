package com.ais.plate_req_api.model

import com.google.gson.annotations.SerializedName

data class Results(

    @SerializedName("box") var box: Box? = Box(),
    @SerializedName("plate") var plate: String? = null,
    @SerializedName("region") var region: Region? = Region(),
    @SerializedName("score") var score: Double? = null,
    @SerializedName("candidates") var candidates: ArrayList<Candidates> = arrayListOf(),
    @SerializedName("dscore") var dscore: Double? = null,
    @SerializedName("vehicle") var vehicle: Vehicle? = Vehicle()

)

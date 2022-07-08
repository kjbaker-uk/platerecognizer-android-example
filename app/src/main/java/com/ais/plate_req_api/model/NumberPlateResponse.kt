package com.ais.plate_req_api.model

import com.google.gson.annotations.SerializedName

data class NumberPlateResponse(

    @SerializedName("processing_time") var processingTime: Double? = null,
    @SerializedName("results") var results: ArrayList<Results> = arrayListOf(),
    @SerializedName("filename") var filename: String? = null,
    @SerializedName("version") var version: Int? = null,
    @SerializedName("camera_id") var cameraId: String? = null,
    @SerializedName("timestamp") var timestamp: String? = null

)

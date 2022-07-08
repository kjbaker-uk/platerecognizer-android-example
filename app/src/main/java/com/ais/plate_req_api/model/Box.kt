package com.ais.plate_req_api.model

import com.google.gson.annotations.SerializedName

data class Box(

    @SerializedName("xmin") var xmin: Int? = null,
    @SerializedName("ymin") var ymin: Int? = null,
    @SerializedName("xmax") var xmax: Int? = null,
    @SerializedName("ymax") var ymax: Int? = null

)

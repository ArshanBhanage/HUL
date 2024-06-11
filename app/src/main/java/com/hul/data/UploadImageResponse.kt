package com.hul.data

data class UploadImageResponse(
    val message: String,
    val error: Boolean,
    val data: UploadImageData
)

data class UploadImageData(
    val url: String
)
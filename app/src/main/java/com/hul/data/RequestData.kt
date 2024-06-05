package com.hul.data

data class RequestData(
    var mobileNo: String? = null,
    var otpFor: String? = null,
    var language: String? = null,
    var otp: String? = null,
    var transactionCode: String? = null,
    var otpAuthorizationCode: String? = null,
    var pin: String? = null,
    var userId: String? = null,
    var customerType: String? = null,
    var customerId: Long = 0,
)

package com.hul.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.Html
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.hul.R
import com.hul.loginRegistraion.LoginRegistrationActivity
import com.hul.user.UserInfo
import io.github.rupinderjeet.kprogresshud.KProgressHUD
import java.io.ByteArrayOutputStream
import java.net.InetAddress
import java.net.NetworkInterface
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*


//Store Preferences constants
const val TERMSANDCONDITIONACCEPTED = "termsAndConditionsAccepted"
const val APP_LANGUAGE = "registered_user"
const val PROFILE_ID = "profile_id"
const val USER_PIC = "userPic"
const val LOGIN_ID = "loginId"
const val ACTION_CONTINUE = "action_continue"
const val CUSTOMER_ID = "customerID"
const val ONBOARDING_PARTNER = "onboardingPartner"
const val PASSWORD = "password"
const val AUTH_TOKEN = "authToken"
const val PENDING_LEAD_DETAIL = "pendingLeadSetails"
const val PROJECT_ID = "projectId"
const val PROJECT_Name = "projectName"
const val LEAD_DETAIL_LIST = "leadListDetails"
const val REFERRAL_SCHEME_CODE = "referralSchemeCode"
const val COMPANY = "company"
const val CITY = "city"
const val USER_NAME = "userName"
const val AADHAAR_MOBILE = "aadhaarMobile"
const val DASHBOARD_RESPONSE = "dashboardResponse"
const val AADHAAR_EMAIL = "aadharEmail"
const val COMPANY_ID = "companyID"
const val DASHBOARD_ID = "dashboardId"
const val MANDATE_FAIL = "mandateFailCount"
const val MANDATE_FAIL_UPI = "mandateFailCountUPI"
const val UBER_LOGIN_BOOLEAN = "uberLoginBoolean"
const val HIT_DASH = "hitDash"
const val LAST_LOGIN = "lastLoginTime"
const val MANDATE_APPLIED = "mandateApplied"
const val DASHBOARD_BOOLEAN = "dashboardBoolean"
const val CONSUMER_ID = "407"
const val CHANNEL_ID = "M"
const val SOURCE = "androidDirect"
const val OS_NAME = "Android"
const val SEND_OTP = "verifyMobile"
const val SEND_OTP_CREATE_CUSTOMER = "createCustomer"
const val SEND_DOCUMENT = "verifyDocument"
const val FORGET_PIN = "forgotPin"
const val OTP_AUTHORIZATION_CODE = "OTPAuthorizationCode"
const val FOR_FORGET_PIN = "forForgetPIN"
const val FOR_REGISTER_PIN = "forRegisterPIN"
const val PIN_FOR = "PINFor"
const val REFERRAL_CODE = "referralCode"
const val CUSTOMER_TYPE = "customerType"
const val MODE_OF_ACC = "CLICK_ACCEPT"
const val LANGUAGE = "en"
const val SUB_COMPANY_LIST = "sub_company_list"
const val COMPANY_DETAIL = "company_detail"
const val LOGIN_MODEL = "loginModel"
const val AADHAAR_DATA_TAG = "PrintLetterBarcodeData"
const val AADHAR_UID_ATTR = "uid"
const val AADHAR_NAME_ATTR = "name"
const val AADHAR_GENDER_ATTR = "gender"
const val AADHAR_YOB_ATTR = "yob"
const val AADHAR_CO_ATTR = "house"
const val AADHAR_VTC_ATTR = "vtc"
const val AADHAR_PO_ATTR = "po"
const val AADHAR_DIST_ATTR = "dist"
const val AADHAR_STATE_ATTR = "state"
const val AADHAR_PC_ATTR = "pc"
const val REDIRECTION_POSITION = "redirectionPosition"
const val DATA = "data"
const val MODEL = "model"
const val PICK_IMAGE = 1
const val PICK_CAMERA = 0
const val LOAN_LIST = "loanList"
const val AADHAAR_URL = "aadhaarUrl"
var about_us = "https://www.mintwalk.com/supermoney/aboutus.html"

var deviceFootPrintHeader = "deviceFPmsgHeader"
var messageHeader = "msgHeader"
var dataTag = "data"

//language list
val languages = arrayListOf("English", "हिंदी", "ಕನ್ನಡ", "தமிழ்")

const val USER_TYPE = "userType"
const val MY_AREA = "myArea"



fun getScreenOrientation(mContext: AppCompatActivity): String {
    val getOrient = mContext.windowManager.defaultDisplay
    var orientation = Configuration.ORIENTATION_UNDEFINED
    var strorient = ""
    if (getOrient.width == getOrient.height) {
        orientation = Configuration.ORIENTATION_SQUARE
    } else {
        if (getOrient.width < getOrient.height) {
            orientation = Configuration.ORIENTATION_PORTRAIT
            strorient = "Portrait"
        } else {
            orientation = Configuration.ORIENTATION_LANDSCAPE
            strorient = "Landscape"
        }
    }
    return strorient
}

fun getIPAddress(useIPv4: Boolean): String {
    try {
        val interfaces: List<NetworkInterface> =
            Collections.list(NetworkInterface.getNetworkInterfaces())
        for (intf: NetworkInterface in interfaces) {
            val addrs: List<InetAddress> = Collections.list(intf.inetAddresses)
            for (addr: InetAddress in addrs) {
                if (!addr.isLoopbackAddress) {
                    val sAddr = addr.hostAddress
                    val isIPv4 = sAddr.indexOf(':') < 0
                    if (useIPv4) {
                        if (isIPv4) return sAddr
                    } else {
                        if (!isIPv4) {
                            val delim = sAddr.indexOf('%') // drop ip6 zone suffix
                            return if (delim < 0) sAddr.uppercase(Locale.getDefault()) else sAddr.substring(
                                0,
                                delim
                            ).uppercase(
                                Locale.getDefault()
                            )
                        }
                    }
                }
            }
        }
    } catch (ex: Exception) {
    } // for now eat exceptions
    return ""
}

fun getConnectionMode(mContext: Context): String {
    val cm = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val info = cm.activeNetworkInfo
    if (info == null || !info.isConnected) return "-" //not connected
    if (info.type == ConnectivityManager.TYPE_WIFI) return "WIFI"
    if (info.type == ConnectivityManager.TYPE_MOBILE) {
        return when (info.subtype) {
            TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN -> "2G"
            TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP -> "3G"
            TelephonyManager.NETWORK_TYPE_LTE -> "4G"
            else -> "?"
        }
    }
    return "?"
}

fun getNetworkProvider(mContext: Context): String {
    val telephonyManager = mContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    return telephonyManager.networkOperatorName
}

@SuppressLint("HardwareIds")
fun getDeviceID(mContext: Context): String {
    return Settings.Secure.getString(mContext.contentResolver, Settings.Secure.ANDROID_ID)
}

private var hud: KProgressHUD? = null

fun setProgressDialog(mContext: Context, msg: String) {
    hud = KProgressHUD.create(mContext)
        .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
        .setLabel("Please wait")
        .setDetailsLabel(msg)
        .setCancellable(false)
        .setAnimationSpeed(2)
        .setDimAmount(0.5f)
        .show()

}

fun cancelProgressDialog() {
    hud?.dismiss()
}

fun noInternetDialogue(mContext: Context, type: Int, retryInterface: RetryInterface) {
    val alertdialog = AlertDialog.Builder(mContext, R.style.AlertDialogTheme).create()
    alertdialog.setCancelable(false)
    alertdialog.setTitle("Error")
    alertdialog.setMessage("No internet connection ")
    alertdialog.setButton(DialogInterface.BUTTON_POSITIVE, "Retry") { _, _ ->
        alertdialog.cancel()
        retryInterface.retry(type)
    }
    alertdialog.show()
}

fun nonredirectionAlertDialogue(mContext: Context, msg: String) {
    val alertdialog = AlertDialog.Builder(mContext, R.style.AlertDialogTheme).create()
    alertdialog!!.setTitle("")
    alertdialog.setMessage(msg)
    alertdialog.setCancelable(false)
    alertdialog.setButton(
        DialogInterface.BUTTON_POSITIVE, mContext.getString(
            R.string.btn_ok
        )
    ) { dialog, which -> dialog.cancel() }

    alertdialog.show()
}

fun redirectToLogin(mContext: Context) {
    val intent = Intent(mContext, LoginRegistrationActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    mContext.startActivity(intent)
}

fun redirectionAlertDialogue(mContext: Context, msg: String) {
    val alertdialog = AlertDialog.Builder(mContext).create()
    alertdialog!!.setTitle("")
    alertdialog.setMessage(msg)
    alertdialog.setCancelable(false)
    alertdialog.setButton(
        DialogInterface.BUTTON_POSITIVE, mContext.getString(
            R.string.btn_ok
        )
    ) { dialog, which -> redirectToLogin(mContext) }
    if (!(mContext as Activity?)!!.isFinishing) alertdialog.show()
}

fun displayMessage(mContext: Context, msg: String) {
    Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show()
}


fun dpToPx(mContext: Context, dp: Int): Int {
    val density = mContext.resources.displayMetrics.density
    return Math.round(dp.toFloat() * density)
}

fun subtractDays(date: Date?, days: Int): Date {
    val cal = GregorianCalendar()
    cal.time = date
    cal.add(Calendar.DATE, -days)
    return cal.time
}

fun addDays(date: Date?, days: Int): Date {
    val cal = GregorianCalendar()
    cal.time = date!!
    cal.add(Calendar.DATE, days)
    return cal.time
}

fun formatCurrency2(amount: Double): String {
    val formatter = DecimalFormat("##,##,###.##")
    return formatter.format(amount)
}

fun convertDpToPixel(dp: Float): Int {
    val metrics = Resources.getSystem().displayMetrics
    val px = dp * (metrics.densityDpi / 160f)
    return Math.round(px)
}

fun formatCurrency3(amount: Double): String {
    if (amount > 0) {
        val formatter = DecimalFormat("##,##,###.##")
        return formatter.format(amount)
    } else {
        return "0"
    }
}

fun formatCurrency(amount: Double): String {
    val formatter = DecimalFormat("##,##,###")
    return formatter.format(amount)
}

fun displayNeverAskAgainDialog(
    mContext: Context,
    name: String,
    listener: RuntimePermissionUtils.PermissionRetryListener
) {
    val builder = AlertDialog.Builder(mContext, R.style.AlertDialogTheme)
    builder.setMessage(
        ("We need " + name + " permission for performing necessary task. Please permit the permission through "
                + "Settings screen.\n\nSelect Permissions -> Enable permission")
    )
    builder.setCancelable(false)
    builder.setPositiveButton("Permit Manually") { dialog, which ->
        dialog.dismiss()
        listener.onRedirect()
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", mContext.packageName, null)
        intent.data = uri
        mContext.startActivity(intent)
    }
    builder.show()
}

fun StringToBitMap(encodedString: String?): Bitmap? {
    try {
        val encodeByte = Base64.decode(encodedString, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
    } catch (e: Exception) {
        e.message
        return null
    }
}

fun BitMapToString(bitmap: Bitmap): String {
    val baos: ByteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
    val b: ByteArray = baos.toByteArray()
    return Base64.encodeToString(b, Base64.DEFAULT)
}

fun numberValidation(number: String): Boolean {
    return number.length == 10
}

fun repaetValidation(number: String): Boolean {
    val char = number.toCharArray().distinct()
    return char.size != 1
}

fun initialLetterValidation(number: String): Boolean {
    val char = number[0]
    return char.digitToInt() > 4
}

fun pinValidation(number: String): Boolean {
    return number.length == 6
}

fun otpValidation(number: String): Boolean {
    return number.length == 6
}
object UserTypes {
    const val MOBILISER = "MOBILISER"
    const val FIELD_AUDITOR = "FIELD_AUDITOR"
}
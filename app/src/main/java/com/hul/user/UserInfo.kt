package com.hul.user

/**
 * Created by Nitin Chorge on 23-11-2020.
 */
import com.hul.storage.Storage
import com.hul.utils.AUTH_TOKEN
import com.hul.utils.LOGIN_ID
import com.hul.utils.MY_AREA
import com.hul.utils.PENDING_LEAD_DETAIL
import com.hul.utils.PROJECT_ID
import com.hul.utils.PROJECT_Name
import com.hul.utils.USER_TYPE
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Handles User lifecycle. Manages registrations, logs in and logs out.
 * Knows when the user is logged in.
 */

@Singleton
class UserInfo @Inject constructor(private val storage: Storage) {

    /**
     *  UserDataRepository is specific to a logged in user. This determines if the user
     *  is logged in or not, when the user logs in, a new instance will be created.
     *  When the user logs out, this will be null.
     */
    //var userDataRepository: UserDataRepository? = null


    var loginId: String
        get() = storage.getString(LOGIN_ID)
        set(value) = storage.setString(LOGIN_ID, value)

    var authToken: String
        get() = storage.getString(AUTH_TOKEN)
        set(value) = storage.setString(AUTH_TOKEN, value)

    var pendingLeadString: String
        get() = storage.getString(PENDING_LEAD_DETAIL)
        set(value) = storage.setString(PENDING_LEAD_DETAIL, value)


    var projectId: String
        get() = storage.getString(PROJECT_ID)
        set(value) = storage.setString(PROJECT_ID, value)

    var projectName: String
        get() = storage.getString(PROJECT_Name)
        set(value) = storage.setString(PROJECT_Name, value)

    var myArea: String
        get() = storage.getString(MY_AREA)
        set(value) = storage.setString(MY_AREA, value)

    var userType: String
        get() = storage.getString(USER_TYPE)
        set(value) = storage.setString(USER_TYPE, value)
}

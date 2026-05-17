package com.example.llm61.auth

import android.app.Activity
import android.content.Context
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.authentication.storage.CredentialsManager
import com.auth0.android.authentication.storage.CredentialsManagerException
import com.auth0.android.authentication.storage.SharedPreferencesStorage
import com.auth0.android.callback.Callback
import com.auth0.android.jwt.JWT
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.example.llm61.BuildConfig

class Auth0Manager(context: Context) {

    private val account: Auth0 = Auth0.getInstance(
        BuildConfig.AUTH0_CLIENT_ID,
        BuildConfig.AUTH0_DOMAIN
    )

    private val credentialsManager = CredentialsManager(
        AuthenticationAPIClient(account),
        SharedPreferencesStorage(context)
    )

    data class UserInfo(val sub: String, val username: String, val email: String)

    private fun decodeUserInfo(credentials: Credentials): UserInfo? {
        val jwt = JWT(credentials.idToken)
        val sub = jwt.subject ?: jwt.getClaim("sub").asString() ?: return null
        if (sub.isBlank()) return null
        val name = jwt.getClaim("nickname").asString() ?: jwt.getClaim("name").asString()
        val email = jwt.getClaim("email").asString() ?: ""
        return UserInfo(
            sub = sub,
            username = name ?: email.substringBefore("@").ifBlank { "User" },
            email = email
        )
    }

    fun tryRestoreSession(onSuccess: (UserInfo) -> Unit, onNoSession: () -> Unit) {
        if (!credentialsManager.hasValidCredentials()) {
            onNoSession()
            return
        }
        credentialsManager.getCredentials(object : Callback<Credentials, CredentialsManagerException> {
            override fun onSuccess(result: Credentials) {
                val info = decodeUserInfo(result)
                if (info != null) {
                    android.util.Log.d("AuthDbg", "Session restored silently: sub='${info.sub}'")
                    onSuccess(info)
                } else {
                    credentialsManager.clearCredentials()
                    onNoSession()
                }
            }
            override fun onFailure(error: CredentialsManagerException) {
                android.util.Log.w("AuthDbg", "Silent restore failed: ${error.message}")
                onNoSession()
            }
        })
    }

    fun login(
        activity: Activity,
        onSuccess: (UserInfo) -> Unit,
        onError: (String) -> Unit,
        onCanceled: () -> Unit = {}
    ) {
        WebAuthProvider.login(account)
            .withScheme("demo")
            .withScope("openid profile email offline_access")
            .withParameters(mapOf("prompt" to "login"))
            .start(activity, object : Callback<Credentials, AuthenticationException> {
                override fun onSuccess(result: Credentials) {
                    android.util.Log.d("AuthDbg", "Auth0 onSuccess fired")
                    credentialsManager.saveCredentials(result)
                    val info = decodeUserInfo(result)
                    if (info == null) {
                        onError("Login succeeded but no user id returned")
                        return
                    }
                    android.util.Log.d("AuthDbg", "JWT decoded: sub='${info.sub}' email='${info.email}'")
                    onSuccess(info)
                }

                override fun onFailure(error: AuthenticationException) {
                    android.util.Log.d("AuthDbg", "Auth0 onFailure: ${error.message} desc=${error.getDescription()}")
                    val description = error.getDescription() ?: ""
                    val isUserCancellation = error.isCanceled
                            || description.contains("did not authorize", ignoreCase = true)
                            || description.contains("denied", ignoreCase = true)
                    if (isUserCancellation) onCanceled()
                    else onError(description.ifBlank { error.message ?: "Login failed" })
                }
            })
    }

    fun logout(activity: Activity, onComplete: () -> Unit) {
        credentialsManager.clearCredentials()
        WebAuthProvider.logout(account)
            .withScheme("demo")
            .start(activity, object : Callback<Void?, AuthenticationException> {
                override fun onSuccess(result: Void?) { onComplete() }
                override fun onFailure(error: AuthenticationException) { onComplete() }
            })
    }
}

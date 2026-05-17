package com.example.llm61.auth

import android.app.Activity
import android.content.Context
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
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

    data class UserInfo(
        val sub: String,
        val username: String,
        val email: String
    )

    fun login(
        activity: Activity,
        onSuccess: (UserInfo) -> Unit,
        onError: (String) -> Unit,
        onCanceled: () -> Unit = {}
    ) {
        WebAuthProvider.login(account)
            .withScheme("demo")
            .withScope("openid profile email")
            .withParameters(mapOf("prompt" to "login"))
            .start(activity, object : Callback<Credentials, AuthenticationException> {
                override fun onSuccess(result: Credentials) {
                    android.util.Log.d("AuthDbg", "Auth0 onSuccess fired")
                    val jwt = JWT(result.idToken)
                    val sub = jwt.subject ?: jwt.getClaim("sub").asString()
                    val name = jwt.getClaim("nickname").asString()
                        ?: jwt.getClaim("name").asString()
                    val email = jwt.getClaim("email").asString() ?: ""
                    android.util.Log.d("AuthDbg", "JWT decoded: sub='$sub' email='$email' name='$name'")

                    if (sub.isNullOrBlank()) {
                        onError("Login succeeded but no user id returned")
                        return
                    }
                    onSuccess(
                        UserInfo(
                            sub = sub,
                            username = name ?: email.substringBefore("@").ifBlank { "User" },
                            email = email
                        )
                    )
                }

                override fun onFailure(error: AuthenticationException) {
                    android.util.Log.d("AuthDbg", "Auth0 onFailure: ${error.message} desc=${error.getDescription()}")
                    val description = error.getDescription() ?: ""
                    val isUserCancellation = error.isCanceled
                            || description.contains("did not authorize", ignoreCase = true)
                            || description.contains("denied", ignoreCase = true)

                    if (isUserCancellation) {
                        onCanceled()
                    } else {
                        onError(description.ifBlank { error.message ?: "Login failed" })
                    }
                }
            })
    }

    fun logout(
        activity: Activity,
        onComplete: () -> Unit
    ) {
        WebAuthProvider.logout(account)
            .withScheme("demo")
            .start(activity, object : Callback<Void?, AuthenticationException> {
                override fun onSuccess(result: Void?) { onComplete() }
                override fun onFailure(error: AuthenticationException) { onComplete() }
            })
    }
}
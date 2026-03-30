package com.seoksumin.voicechat.data.prefs



import android.content.Context

object AppPrefs {
    private const val PREFS_NAME = "voicechat_prefs"
    private const val KEY_ONBOARDING_DONE = "onboarding_done"
    private const val KEY_LOGGED_IN = "logged_in"

    private const val KEY_ACCESS_TOKEN = "access_token"


    fun isOnboardingDone(context: Context): Boolean {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sp.getBoolean(KEY_ONBOARDING_DONE, false)
    }

    fun setOnboardingDone(context: Context, done: Boolean) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit().putBoolean(KEY_ONBOARDING_DONE, done).apply()
    }
    fun isLoggedIn(context: Context): Boolean {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sp.getBoolean(KEY_LOGGED_IN, false)
    }
    fun setLoggedIn(context: Context, loggedIn: Boolean) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit().putBoolean(KEY_LOGGED_IN, loggedIn).apply()
    }

    fun saveToken(context: Context, token: String) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun getToken(context: Context): String? {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sp.getString(KEY_ACCESS_TOKEN, null)
    }

    fun clearToken(context: Context) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit().remove(KEY_ACCESS_TOKEN).apply()
    }

    fun logout(context: Context) {
        clearToken(context)
        setLoggedIn(context, false)
    }


}

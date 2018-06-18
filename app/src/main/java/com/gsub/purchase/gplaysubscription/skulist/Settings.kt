package com.gsub.purchase.gplaysubscription.skulist

import android.content.Context
import android.content.SharedPreferences

object Settings {

  const val SHARED_PREF_NAME = "SUBSCRIPTION_PREFERENCES"

  fun getSharedPrefs(context: Context, name: String = SHARED_PREF_NAME): SharedPreferences {
    return context.getSharedPreferences(name, Context.MODE_PRIVATE)
  }

}
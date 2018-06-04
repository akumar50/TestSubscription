package com.gsub.purchase.gplaysubscription

import android.content.Context.MODE_PRIVATE
import android.support.annotation.DrawableRes
import android.util.Log


class MainViewController {
  private val TAG = "MainViewController"

  // Graphics for the gas gauge
  private val TANK_RES_IDS = intArrayOf(R.drawable.gas0, R.drawable.gas1, R.drawable.gas2,
      R.drawable.gas3, R.drawable.gas4)

  // How many units (1/4 tank is our unit) fill in the tank.
  private val TANK_MAX = 4

  private lateinit var mActivity: MainActivity

  // Current amount of gas in tank, in units
  private var mTank: Int = 0

  fun MainViewController(activity: MainActivity) {
    mActivity = activity
    loadData()
  }

  fun useGas() {
    mTank--
    saveData()
    Log.d(TAG, "Tank is now: $mTank")
  }

  fun isTankEmpty(): Boolean {
    return mTank <= 0
  }

  fun isTankFull(): Boolean {
    return mTank >= TANK_MAX
  }

  @DrawableRes
  fun getTankResId(): Int {
    val index = if (mTank >= TANK_RES_IDS.size) TANK_RES_IDS.size - 1 else mTank
    return TANK_RES_IDS[index]
  }

  /**
   * Save current tank level to disc
   *
   * Note: In a real application, we recommend you save data in a secure way to
   * prevent tampering.
   * For simplicity in this sample, we simply store the data using a
   * SharedPreferences.
   */
  private fun saveData() {
    val spe = mActivity.getPreferences(MODE_PRIVATE).edit()
    spe.putInt("tank", mTank)
    spe.apply()
    Log.d(TAG, "Saved data: tank = " + mTank.toString())
  }

  private fun loadData() {
    val sp = mActivity.getPreferences(MODE_PRIVATE)
    mTank = sp.getInt("tank", 2)
    Log.d(TAG, "Loaded data: tank = " + mTank.toString())
  }
}
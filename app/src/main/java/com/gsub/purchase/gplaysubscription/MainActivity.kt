package com.gsub.purchase.gplaysubscription

import android.accounts.AccountManager
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.support.annotation.StringRes
import android.support.annotation.UiThread
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.ImageView
import com.android.billingclient.api.Purchase

import com.gsub.purchase.gplaysubscription.billing.BillingManager
import com.gsub.purchase.gplaysubscription.billing.BillingManager.SendEmailResponse
import com.gsub.purchase.gplaysubscription.billing.BillingProvider
import com.gsub.purchase.gplaysubscription.skulist.AcquireFragment

class MainActivity : FragmentActivity(), BillingProvider, SendEmailResponse {

  // Debug tag, for logging
  private val TAG = "GamePlayActivity"

  // Tag for a dialog that allows us to find it when screen was rotated
  private val DIALOG_TAG = "dialog"

  private var mBillingManager: BillingManager? = null
  private var mAcquireFragment: AcquireFragment? = null
  private var mViewController: MainViewController? = null

  private var mScreenWait: View? = null
  private var mScreenMain:View? = null
  //private ImageView mCarImageView;
  private var mGasImageView: ImageView? = null

  private var mButtonPurchase: View? = null
  private var mButtonDrive:View? = null

  protected override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_game_play)

    // Start the controller and load game data
    mViewController = MainViewController()

    // Try to restore dialog fragment if we were showing it prior to screen rotation
    if (savedInstanceState != null) {
      mAcquireFragment = getSupportFragmentManager()
          .findFragmentByTag(DIALOG_TAG) as AcquireFragment
    }

    // Create and initialize BillingManager which talks to BillingLibrary
    mBillingManager = BillingManager(this)

    mScreenWait = findViewById(R.id.screen_wait)
    mScreenMain = findViewById(R.id.screen_main)
    mGasImageView = findViewById(R.id.gas_gauge) as ImageView

    // Specify purchase and drive buttons listeners
    // Note: This couldn't be done inside *.xml for Android TV since TV layout is inflated
    // via AppCompat
    mButtonPurchase = findViewById(R.id.button_purchase)
    mButtonPurchase!!.setOnClickListener(
            View.OnClickListener { view -> onPurchaseButtonClicked(view) })
    mButtonDrive = findViewById(R.id.button_drive)
    mButtonDrive!!.setOnClickListener(
            View.OnClickListener { view -> onDriveButtonClicked(view) })
    showRefreshedUi()
  }

  override val billingManager: BillingManager
    get() = mBillingManager!!

  /**
   * User clicked the "Buy Gas" button - show a purchase dialog with all available SKUs
   */
  fun onPurchaseButtonClicked(arg0: View) {
    Log.d(TAG, "Purchase button clicked.")

    if (mAcquireFragment == null) {
      mAcquireFragment = AcquireFragment()
    }

    if (!isAcquireFragmentShown()) {
      mAcquireFragment!!.show(getSupportFragmentManager(), DIALOG_TAG)
    }
  }

  /**
   * Drive button clicked. Burn gas!
   */
  fun onDriveButtonClicked(arg0: View) {
    Log.d(TAG, "Drive button clicked.")

    if (mViewController!!.isTankEmpty()) {
      alert(R.string.alert_no_gas)
    } else {
      mViewController!!.useGas()
      alert(R.string.alert_drove)
      updateUi()
    }
  }

  /**
   * Remove loading spinner and refresh the UI
   */
  fun showRefreshedUi() {
    setWaitScreen(false)
    updateUi()

    if (isAcquireFragmentShown()) {
      mAcquireFragment!!.refreshUI()
    }
  }

  /**
   * Show an alert dialog to the user
   * @param messageId String id to display inside the alert dialog
   */
  @UiThread
  internal fun alert(@StringRes messageId: Int) {
    alert(messageId, null)
  }

  /**
   * Show an alert dialog to the user
   * @param messageId String id to display inside the alert dialog
   * @param optionalParam Optional attribute for the string
   */
  @UiThread
  internal fun alert(@StringRes messageId: Int, optionalParam: Any?) {
    if (Looper.getMainLooper().thread !== Thread.currentThread()) {
      throw RuntimeException("Dialog could be shown only from the main thread")
    }

    val bld = AlertDialog.Builder(this)
    bld.setNeutralButton("OK", null)

    if (optionalParam == null) {
      bld.setMessage(messageId)
    } else {
      bld.setMessage(getResources().getString(messageId, optionalParam))
    }

    bld.create().show()
  }

  /**
   * Enables or disables the "please wait" screen.
   */
  private fun setWaitScreen(set: Boolean) {
    mScreenMain!!.setVisibility(if (set) View.GONE else View.VISIBLE)
    mScreenWait!!.visibility = if (set) View.VISIBLE else View.GONE
  }

  /**
   * Update UI to reflect model
   */
  @UiThread
  private fun updateUi() {
    Log.d(TAG, "Updating the UI. Thread: " + Thread.currentThread().name)

    // Update gas gauge to reflect tank status
    mGasImageView!!.setImageResource(mViewController!!.getTankResId())
  }

  fun isAcquireFragmentShown(): Boolean {
    return mAcquireFragment != null && mAcquireFragment!!.isVisible()
  }

  protected override fun onDestroy() {
    super.onDestroy()
    mBillingManager!!.destroy()
  }


  override fun sendEmail(purchases: List<Purchase>?) {
    triggerEmailIntent(purchases)
  }

  fun triggerEmailIntent(purchases: List<Purchase>?) {
    val emailIntent = Intent(Intent.ACTION_SEND)
    emailIntent.data = Uri.parse(getEmailId())
    emailIntent.type = "text/plain"
    emailIntent.putExtra(Intent.EXTRA_EMAIL, getEmailId())
    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Purchase Response")
    emailIntent.putExtra(Intent.EXTRA_TEXT, purchases.toString())
    try {
      startActivity(emailIntent)
    } catch (ex: android.content.ActivityNotFoundException) {
      Log.d(TAG,"There is no email client installed")
    }

  }

  fun getEmailId() : String {
    var email: String? = null

    val gmailPattern = Patterns.EMAIL_ADDRESS
    val accounts = AccountManager.get(this).accounts
    for (account in accounts) {
      if (gmailPattern.matcher(account.name).matches()) {
        email = account.name
      }
    }
    return email!!
  }
}


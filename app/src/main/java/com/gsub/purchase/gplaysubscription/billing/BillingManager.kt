package com.gsub.purchase.gplaysubscription.billing

import android.app.Activity
import android.content.ContentValues
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetailsParams
import com.android.billingclient.api.SkuDetailsResponseListener
import com.gsub.purchase.gplaysubscription.MainActivity
import java.util.Arrays
import java.util.HashMap

class BillingManager(mainActivity: MainActivity) : PurchasesUpdatedListener {

  private var sendEmailResponse: SendEmailResponse? = null

  private val TAG = "BillingManager"
  //private var mBillingClient: BillingClient? = null
  private var mActivity: Activity = mainActivity

  val mBillingClient = BillingClient.newBuilder(mActivity).setListener(this).build()

  init {
    if (mActivity is SendEmailResponse) {
      sendEmailResponse = mainActivity
    }
  }

  fun startPurchaseFlow(skuId: String, billingType: String) {
    // Specify a runnable to start when connection to Billing client is established
    mBillingClient.startConnection(object : BillingClientStateListener {
      override fun onBillingSetupFinished(@BillingClient.BillingResponse billingResponse: Int) {
        if (billingResponse == BillingClient.BillingResponse.OK) {
          Log.i(ContentValues.TAG, "onBillingSetupFinished() response: $billingResponse")
        } else {
          Log.w(ContentValues.TAG, "onBillingSetupFinished() error code: $billingResponse")
        }
      }

      override fun onBillingServiceDisconnected() {
        Log.w(ContentValues.TAG, "onBillingServiceDisconnected()")
      }
    })
    val executeOnConnectedService = Runnable {
      val billingFlowParams = BillingFlowParams.newBuilder()
          .setType(billingType)
          .setSku(skuId)
          .build()
//      val billingFlowParams = BillingFlowParams.newBuilder()
//          .setSku(skuId)
//          .setType(billingType)
//          .setOldSku("sub4")
//          .setReplaceSkusProrationMode(IMMEDIATE_WITHOUT_PRORATION)
//          .build()
      mBillingClient.launchBillingFlow(mActivity, billingFlowParams)
    }

    // If Billing client was disconnected, we retry 1 time
    // and if success, execute the query
    startServiceConnectionIfNeeded(executeOnConnectedService)
  }

  companion object {
    val skuDetails = HashMap<String, List<String>>()
  }
  init {
    //skuDetails.put(BillingClient.SkuType.INAPP, Arrays.asList<String>("gas", "premium"))
    skuDetails.put(BillingClient.SkuType.SUBS, Arrays.asList<String>("sub1", "sub2", "sub3", "sub4", "gold_premium","gold_monthly", "gold_yearly"))
  }

  fun getSkus(@BillingClient.SkuType type: String): List<String> {
    return skuDetails[type]!!
  }

  override fun onPurchasesUpdated(responseCode: Int, purchases: List<Purchase>?) {
    Log.d(TAG, "onPurchasesUpdated() response: $responseCode $purchases")
    if (responseCode == 0)
      sendEmailResponse?.sendEmail(purchases)
  }

  fun querySkuDetailsAsync(@BillingClient.SkuType itemType: String,
      skuList: List<String>, listener: SkuDetailsResponseListener) {
    // Specify a runnable to start when connection to Billing client is established
    val executeOnConnectedService = Runnable {
      val skuDetailsParams = SkuDetailsParams.newBuilder()
          .setSkusList(skuList).setType(itemType).build()
      mBillingClient?.querySkuDetailsAsync(skuDetailsParams
      ) { responseCode, skuDetailsList ->
        listener.onSkuDetailsResponse(responseCode, skuDetailsList)
      }
    }

    // If Billing client was disconnected, we retry 1 time and if success, execute the query
    startServiceConnectionIfNeeded(executeOnConnectedService)
  }

  private fun startServiceConnectionIfNeeded(executeOnSuccess: Runnable?) {
    if (mBillingClient.isReady) {
      executeOnSuccess?.run()
    } else {
      mBillingClient.startConnection(object : BillingClientStateListener {
        override fun onBillingSetupFinished(@BillingClient.BillingResponse billingResponse: Int) {
          if (billingResponse == BillingClient.BillingResponse.OK) {
            Log.i(TAG, "onBillingSetupFinished() response: $billingResponse")
            executeOnSuccess?.run()
          } else {
            Log.w(TAG, "onBillingSetupFinished() error code: $billingResponse")
          }
        }

        override fun onBillingServiceDisconnected() {
          Log.w(TAG, "onBillingServiceDisconnected()")
        }
      })
    }
  }

  fun destroy() {
    mBillingClient?.endConnection()
  }

  interface SendEmailResponse {
    fun sendEmail(purchases: List<Purchase>?)
  }

}

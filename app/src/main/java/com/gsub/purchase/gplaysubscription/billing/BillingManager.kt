package com.gsub.purchase.gplaysubscription.billing

import android.app.Activity
import android.content.ContentValues
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponse
import com.android.billingclient.api.BillingClient.FeatureType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchaseHistoryResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetailsParams
import com.android.billingclient.api.SkuDetailsResponseListener
import com.gsub.purchase.gplaysubscription.MainActivity
import com.gsub.purchase.gplaysubscription.skulist.GConstants
import com.gsub.purchase.gplaysubscription.skulist.Security
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
//        val billingFlowParams = BillingFlowParams.newBuilder()
//            .setSku(skuId)
//            .setType(billingType)
//            .setOldSku("")
//            .build()
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
    skuDetails.put(BillingClient.SkuType.SUBS,
        Arrays.asList<String>("cricut_weekly", "cricut_bronze", "cricut_silver",
            "cricut_gold", "cricut_diamond", "cricut_premium", "cricut_seasonal"))
  }

  fun getSkus(@BillingClient.SkuType type: String): List<String> {
    return skuDetails[type]!!
  }

  override fun onPurchasesUpdated(responseCode: Int, purchases: List<Purchase>?) {
    Log.d(TAG, "onPurchasesUpdated() response: $responseCode $purchases")
    if (responseCode == BillingClient.BillingResponse.OK) {
      if (purchases != null) {
        for (purchase in purchases) {
          handlePurchase(purchase)
        }
      }
    }
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

  fun isFeatureSupported(@FeatureType featureType: String) {
    val supported = mBillingClient.isFeatureSupported(featureType)
    Log.d("purchaseSupported : ", ""+supported)
  }

  fun queryPurchases(@BillingClient.SkuType itemType: String){
    val queryPurchase = mBillingClient.queryPurchases(itemType)
    if (queryPurchase.getResponseCode() == BillingResponse.OK) {
      if(queryPurchase.getPurchasesList()!=null && queryPurchase.getPurchasesList()!=null){
        Log.d("purchaseQuery : ", ""+queryPurchase.purchasesList)
      }
    }
  }

  fun queryPurchaseHistoryAsync(@BillingClient.SkuType itemType: String,
      listener: PurchaseHistoryResponseListener) {
    mBillingClient.queryPurchaseHistoryAsync(itemType, listener)
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

  private fun handlePurchase(purchase: Purchase) {
    if (!verifyValidSignature(purchase.originalJson, purchase.signature)) {
      return
    }
  }

  private fun verifyValidSignature(signedData: String, signature: String): Boolean {
    return Security.verifyPurchase(GConstants.GPLAY_BILLING_LINCENCE, signedData,
        signature)
  }

}

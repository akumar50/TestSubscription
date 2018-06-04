package com.gsub.purchase.gplaysubscription.skulist

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.SkuDetailsResponseListener
import com.gsub.purchase.gplaysubscription.R


import com.gsub.purchase.gplaysubscription.billing.BillingProvider
import com.gsub.purchase.gplaysubscription.skulist.row.SkuRowData
import java.util.ArrayList

class AcquireFragment: DialogFragment() {
  private val TAG = "AcquireFragment"

  private var mRecyclerView: RecyclerView? = null
  private var mAdapter: SkusAdapter? = null
  private var mLoadingView: View? = null
  private var mErrorTextView: TextView? = null
  private var mBillingProvider: BillingProvider? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    val root = inflater.inflate(R.layout.acquire_fragment, container, false)
    mErrorTextView = root.findViewById(R.id.error_textview)
    mRecyclerView = root.findViewById(R.id.list) as RecyclerView
    mLoadingView = root.findViewById(R.id.screen_wait)
    // Setup a toolbar for this fragment
    val toolbar = root.findViewById(R.id.toolbar) as Toolbar
    toolbar.setNavigationIcon(R.drawable.ic_arrow_up)
    toolbar.setNavigationOnClickListener { dismiss() }
    toolbar.setTitle(R.string.button_purchase)
    setWaitScreen(true)
    onManagerReady(activity as BillingProvider)
    return root
  }

  /**
   * Refreshes this fragment's UI
   */
  fun refreshUI() {
    Log.d(TAG, "Looks like purchases list might have been updated - refreshing the UI")
    if (mAdapter != null) {
      mAdapter!!.notifyDataSetChanged()
    }
  }

  /**
   * Notifies the fragment that billing manager is ready and provides a BillingProvider
   * instance to access it
   */
  fun onManagerReady(billingProvider: BillingProvider) {
    mBillingProvider = billingProvider
    if (mRecyclerView != null) {
      mAdapter = SkusAdapter(mBillingProvider)
      if (mRecyclerView!!.getAdapter() == null) {
        mRecyclerView!!.setAdapter(mAdapter)
        mRecyclerView!!.setLayoutManager(LinearLayoutManager(getContext()))
      }
      handleManagerAndUiReady()
    }
  }

  /**
   * Enables or disables "please wait" screen.
   */
  private fun setWaitScreen(set: Boolean) {
    mRecyclerView!!.setVisibility(if (set) View.GONE else View.VISIBLE)
    mLoadingView!!.visibility = if (set) View.VISIBLE else View.GONE
  }

  /**
   * Executes query for SKU details at the background thread
   */
  private fun handleManagerAndUiReady() {
    val inList = ArrayList<SkuRowData>()
    val responseListener = SkuDetailsResponseListener { responseCode, skuDetailsList ->
      if (responseCode == BillingClient.BillingResponse.OK && skuDetailsList != null) {
        // Repacking the result for an adapter
        for (details in skuDetailsList) {
          Log.i(TAG, "Found sku: $details")
          inList.add(SkuRowData(details.sku, details.title,
              details.price, details.description,
              details.type))
        }
        if (inList.size == 0) {
          displayAnErrorIfNeeded()
        } else {
          mAdapter!!.updateData(inList)
          setWaitScreen(false)
        }
      }
    }

    // Start querying for in-app SKUs
//    var skus = mBillingProvider!!.billingManager.getSkus(BillingClient.SkuType.INAPP)
//    mBillingProvider!!.billingManager.querySkuDetailsAsync(BillingClient.SkuType.INAPP, skus,
//        responseListener)
    // Start querying for subscriptions SKUs
    val skus = mBillingProvider!!.billingManager.getSkus(BillingClient.SkuType.SUBS)
    mBillingProvider!!.billingManager.querySkuDetailsAsync(BillingClient.SkuType.SUBS, skus,
        responseListener)
  }

  private fun displayAnErrorIfNeeded() {
    if (getActivity() == null || getActivity()!!.isFinishing()) {
      Log.i(TAG, "No need to show an error - activity is finishing already")
      return
    }

    mLoadingView!!.visibility = View.GONE
    mErrorTextView!!.visibility = View.VISIBLE
    mErrorTextView!!.setText(getText(R.string.error_codelab_not_finished))

    // TODO: Here you will need to handle various respond codes from BillingManager
  }
}
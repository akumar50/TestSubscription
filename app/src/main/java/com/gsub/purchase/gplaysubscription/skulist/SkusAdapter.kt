package com.gsub.purchase.gplaysubscription.skulist

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.gsub.purchase.gplaysubscription.R

import com.gsub.purchase.gplaysubscription.billing.BillingProvider
import com.gsub.purchase.gplaysubscription.skulist.row.RowViewHolder
import com.gsub.purchase.gplaysubscription.skulist.row.SkuRowData

class SkusAdapter(mBillingProvider: BillingProvider?) : RecyclerView.Adapter<RowViewHolder>(), RowViewHolder.OnButtonClickListener {
  private var mListData: List<SkuRowData>? = null
  private var mBillingProvider: BillingProvider? = mBillingProvider

  internal fun updateData(data: List<SkuRowData>) {
    mListData = data
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowViewHolder {
    val item = LayoutInflater.from(parent.context)
        .inflate(R.layout.sku_details_row, parent, false)
    return RowViewHolder(item, this)
  }

  override fun onBindViewHolder(holder: RowViewHolder, position: Int) {
    val data = getData(position)
    if (data != null) {
      holder.title.setText(data.title)
      holder.description.setText(data.description)
      holder.price.setText(data.price)
      holder.button?.setEnabled(true)
    }
    when (data!!.sku) {
      "cricut_weekly", "cricut_bronze", "cricut_silver", "cricut_gold", "cricut_diamond",
      "cricut_premium", "cricut_seasonal" -> holder.skuIcon.setImageResource(R.drawable.gold_icon)
    }
  }

  override fun getItemCount(): Int {
    return if (mListData == null) 0 else mListData!!.size
  }

  override fun onButtonClicked(position: Int) {
    val data = getData(position)
    mBillingProvider?.billingManager?.startPurchaseFlow(data!!.sku,
        data.billingType)

  }

  private fun getData(position: Int): SkuRowData? {
    return if (mListData == null) null else mListData!![position]
  }
}


package com.gsub.purchase.gplaysubscription.skulist.row

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.gsub.purchase.gplaysubscription.R


class RowViewHolder(itemView: View, clickListener: OnButtonClickListener) : RecyclerView.ViewHolder(
    itemView) {
  var title: TextView
  var description: TextView
  var price: TextView
  var button: Button? = null
  var skuIcon: ImageView

  /**
   * Handler for a button click on particular row
   */
  interface OnButtonClickListener {
    fun onButtonClicked(position: Int)
  }

  init {
    title = itemView.findViewById(R.id.title) as TextView
    price = itemView.findViewById(R.id.price) as TextView
    description = itemView.findViewById(R.id.description) as TextView
    skuIcon = itemView.findViewById(R.id.sku_icon) as ImageView
    button = itemView.findViewById(R.id.state_button) as Button
    if (button != null) {
      button?.setOnClickListener { clickListener.onButtonClicked(getAdapterPosition()) }
    }
  }
}

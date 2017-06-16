package com.example.android.autofillframework.app

import android.content.Context
import android.graphics.PorterDuff
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.example.android.autofillframework.R
import kotlinx.android.synthetic.main.navigation_item.view.buttonLabel
import kotlinx.android.synthetic.main.navigation_item.view.cardView
import kotlinx.android.synthetic.main.navigation_item.view.infoButton

class NavigationItem @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.NavigationItem,
                defStyleAttr, 0)
        val labelText = typedArray.getString(R.styleable.NavigationItem_labelText)
        val infoText = typedArray.getString(R.styleable.NavigationItem_infoText)
        val logoDrawable = typedArray.getDrawable(R.styleable.NavigationItem_itemLogo)
        @ColorRes val colorRes = typedArray.getResourceId(R.styleable.NavigationItem_imageColor, 0)
        val imageColor = ContextCompat.getColor(getContext(), colorRes)
        typedArray.recycle()
        LayoutInflater.from(context).inflate(R.layout.navigation_item, this)
        logoDrawable?.setColorFilter(imageColor, PorterDuff.Mode.SRC_IN)
        buttonLabel.text = labelText
        buttonLabel.setCompoundDrawablesRelativeWithIntrinsicBounds(logoDrawable, null, null, null)
        infoButton.setOnClickListener {
            AlertDialog.Builder(this@NavigationItem.context)
                    .setMessage(infoText).create().show()
        }
        infoButton.setColorFilter(imageColor)
    }

    fun setNavigationButtonClickListener(l: View.OnClickListener?) {
        cardView.setOnClickListener(l)
    }
}

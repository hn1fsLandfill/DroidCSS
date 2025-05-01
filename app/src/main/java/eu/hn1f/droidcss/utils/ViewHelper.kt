package eu.hn1f.droidcss.utils

import android.content.Context
import android.util.TypedValue

object ViewHelper {
    fun Context.toPx(dp: Int): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        resources.displayMetrics
    ).toInt()
}
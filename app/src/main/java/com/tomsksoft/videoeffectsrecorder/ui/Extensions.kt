package com.tomsksoft.videoeffectsrecorder.ui

import android.content.res.Resources

fun Float.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
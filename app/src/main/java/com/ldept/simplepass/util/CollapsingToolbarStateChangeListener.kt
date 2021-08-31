package com.ldept.simplepass.util

import android.view.View
import com.google.android.material.appbar.AppBarLayout
import kotlin.math.abs

abstract class CollapsingToolbarStateChangeListener : AppBarLayout.OnOffsetChangedListener {
    enum class State {
        EXPANDED,
        COLLAPSED,
    }

    private var currentState : State = State.COLLAPSED

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        if (abs(verticalOffset) >= appBarLayout!!.totalScrollRange) {
            onStateChanged(appBarLayout, State.COLLAPSED)
        } else {
            onStateChanged(appBarLayout, State.EXPANDED)
        }
    }


    abstract fun onStateChanged(appBarLayout: AppBarLayout?, currentState : State)
}